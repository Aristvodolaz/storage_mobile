package com.komus.sorage_mobile.data.offline

import android.util.Log
import com.google.gson.Gson
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.PendingOperationDao
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.request.PickFromLocationRequest
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class OfflineOpType {
    PLACE_BUFFER,
    MOVE,
    PICK_PRODUCT,
    PICK_LOCATION,
    PICK_LOCATION_SKLAD
}

/**
 * Очередь записей, которые нельзя отправить сразу из-за отсутствия связи.
 *
 * Каждая операция получает ключ идемпотентности до первой отправки. Если ответ потерян и операция
 * уходит повторно, сервер узнаёт ключ и не выполняет её второй раз (см. middleware Idempotency-Key на сервере).
 */
@Singleton
class OfflineQueue @Inject constructor(
    private val api: StorageApi,
    private val dao: PendingOperationDao,
    private val scheduler: DrainScheduler
) {
    private val gson = Gson()

    // Разовый и периодический воркеры могут идти параллельно: иначе одна операция уйдёт дважды
    private val drainMutex = Mutex()

    fun pendingCount(): Flow<Int> = dao.pendingCount()

    fun failedCount(): Flow<Int> = dao.failedCount()

    /** Операции, которые сервер отклонил или которые не удалось отправить из-за ошибки сервера. */
    fun failedOperations(): Flow<List<PendingOperationEntity>> = dao.getFailedFlow()

    /** Вернуть отклонённую операцию в очередь и попробовать отправить снова. */
    suspend fun retry(id: Long) {
        dao.resetToPending(id)
        scheduler.requestDrain()
    }

    /** Удалить отклонённую операцию без отправки. */
    suspend fun discard(id: Long) {
        dao.delete(id)
    }

    /**
     * Выполняет [send] с ключом идемпотентности; при отсутствии связи сохраняет операцию с тем же ключом
     * и возвращает ответ с [QUEUED_MESSAGE]. Ошибки сервера (HTTP и success=false) возвращаются как есть.
     */
    suspend fun sendOrQueue(
        type: OfflineOpType,
        productId: String?,
        body: Any,
        send: suspend (idempotencyKey: String) -> BaseResponse
    ): BaseResponse {
        val key = UUID.randomUUID().toString()
        return try {
            send(key)
        } catch (e: IOException) {
            Log.w(TAG, "Нет связи, операция ${type.name} сохранена в очередь: ${e.message}")
            dao.insert(
                PendingOperationEntity(
                    type = type.name,
                    productId = productId,
                    body = gson.toJson(body),
                    createdAt = System.currentTimeMillis(),
                    idempotencyKey = key
                )
            )
            scheduler.requestDrain()
            BaseResponse(success = true, message = QUEUED_MESSAGE)
        }
    }

    /**
     * Отправляет накопленные операции по очереди.
     * @return true, если очередь обработана; false, если связь пропала и надо повторить позже.
     */
    suspend fun drain(): Boolean = drainMutex.withLock { drainLocked() }

    private suspend fun drainLocked(): Boolean {
        for (op in dao.getPending()) {
            // Операции, созданные до версии с ключами, получают ключ сейчас: до этого их не отправляли
            // Ключ сохраняем до отправки, чтобы повтор отправил тот же ключ
            val key = op.idempotencyKey ?: UUID.randomUUID().toString().also { dao.assignKey(op.id, it) }
            try {
                val response = execute(op, key)
                if (response.success) {
                    dao.delete(op.id)
                } else {
                    Log.e(TAG, "Сервер отклонил операцию ${op.id}: ${response.message}")
                    dao.markFailed(op.id, response.message ?: "Сервер отклонил операцию")
                }
            } catch (e: IOException) {
                Log.w(TAG, "Связь пропала на операции ${op.id}, повтор позже")
                dao.markAttempt(op.id, e.message)
                return false
            } catch (e: HttpException) {
                val message = "HTTP ${e.code()}"
                if (e.code() >= 500) {
                    // Ошибка на стороне сервера: операция не теряется, повторим позже, порядок сохраняется
                    dao.markAttempt(op.id, message)
                    return false
                }
                Log.e(TAG, "Сервер отклонил операцию ${op.id}: $message")
                dao.markFailed(op.id, message)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка операции ${op.id}", e)
                dao.markFailed(op.id, e.message ?: "Неизвестная ошибка")
            }
        }
        return true
    }

    private suspend fun execute(op: PendingOperationEntity, key: String): BaseResponse {
        return when (OfflineOpType.valueOf(op.type)) {
            OfflineOpType.PLACE_BUFFER -> api.placeProductToBuffer(
                op.productId.orEmpty(),
                gson.fromJson(op.body, PlaceProductRequest::class.java),
                key
            )
            OfflineOpType.MOVE -> api.moveProduct(
                op.productId.orEmpty(),
                gson.fromJson(op.body, MoveProductRequest::class.java),
                key
            )
            OfflineOpType.PICK_PRODUCT -> api.pickProduct(
                op.productId.orEmpty(),
                gson.fromJson(op.body, PickRequest::class.java),
                key
            )
            OfflineOpType.PICK_LOCATION -> api.pickFromLocation(
                gson.fromJson(op.body, PickFromLocationRequest::class.java),
                key
            )
            OfflineOpType.PICK_LOCATION_SKLAD -> api.pickFromLocationBySkladId(
                gson.fromJson(op.body, PickFromLocationRequest::class.java),
                key
            )
        }
    }

    companion object {
        const val QUEUED_MESSAGE = "Сохранено на устройстве, будет отправлено при появлении сети"
        private const val TAG = "OfflineQueue"

        fun isQueued(response: BaseResponse): Boolean = response.message == QUEUED_MESSAGE
    }
}
