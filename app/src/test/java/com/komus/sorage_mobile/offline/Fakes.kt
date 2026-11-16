package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.api.InventoryRequest
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.CacheDao
import com.komus.sorage_mobile.data.db.dao.PendingOperationDao
import com.komus.sorage_mobile.data.db.entity.CacheEntity
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.model.StorageResponse
import com.komus.sorage_mobile.data.offline.DrainScheduler
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.request.PickFromLocationRequest
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.ArticleInventoryResponse
import com.komus.sorage_mobile.data.response.AuthResponse
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.EmptyCellsResponse
import com.komus.sorage_mobile.data.response.LocationDetailsResponse
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.data.response.LocationProductsResponse
import com.komus.sorage_mobile.data.response.ProductInfoResponse
import com.komus.sorage_mobile.data.response.ProductSearchResponse
import com.komus.sorage_mobile.data.response.SearchResponse
import com.komus.sorage_mobile.data.response.UnitResponse
import com.komus.sorage_mobile.util.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Response

/** Хранилище очереди в памяти; ведёт себя как таблица Room. */
class FakePendingOperationDao : PendingOperationDao {
    val rows = mutableListOf<PendingOperationEntity>()
    private var nextId = 1L

    private fun update(id: Long, transform: (PendingOperationEntity) -> PendingOperationEntity) {
        val index = rows.indexOfFirst { it.id == id }
        if (index >= 0) rows[index] = transform(rows[index])
    }

    override suspend fun insert(operation: PendingOperationEntity): Long {
        val id = nextId++
        rows.add(operation.copy(id = id))
        return id
    }

    override suspend fun getPending(): List<PendingOperationEntity> =
        rows.filter { it.status == PendingOperationEntity.STATUS_PENDING }.sortedBy { it.id }

    override fun getFailedFlow(): Flow<List<PendingOperationEntity>> =
        flowOf(rows.filter { it.status == PendingOperationEntity.STATUS_FAILED }.sortedBy { it.id })

    override suspend fun delete(id: Long) {
        rows.removeAll { it.id == id }
    }

    override suspend fun markFailed(id: Long, error: String?) =
        update(id) { it.copy(status = PendingOperationEntity.STATUS_FAILED, attempts = it.attempts + 1, lastError = error) }

    override suspend fun markAttempt(id: Long, error: String?) =
        update(id) { it.copy(attempts = it.attempts + 1, lastError = error) }

    override suspend fun assignKey(id: Long, key: String) =
        update(id) { if (it.idempotencyKey == null) it.copy(idempotencyKey = key) else it }

    override suspend fun resetToPending(id: Long) =
        update(id) { it.copy(status = PendingOperationEntity.STATUS_PENDING, lastError = null) }

    override fun pendingCount(): Flow<Int> =
        flowOf(rows.count { it.status == PendingOperationEntity.STATUS_PENDING })

    override fun failedCount(): Flow<Int> =
        flowOf(rows.count { it.status == PendingOperationEntity.STATUS_FAILED })
}

class FakeCacheDao : CacheDao {
    val entries = mutableMapOf<String, CacheEntity>()

    override suspend fun put(entry: CacheEntity) {
        entries[entry.cacheKey] = entry
    }

    override suspend fun get(key: String): CacheEntity? = entries[key]
}

class FakeDrainScheduler : DrainScheduler {
    var requests = 0

    override fun requestDrain() {
        requests++
    }
}

class FakeSession(
    private val userName: String = "user",
    private val skladId: String = "1383"
) : UserSession {
    override fun getUserName(): String = userName
    override fun getSkladId(): String = skladId
}

/**
 * Заглушка StorageApi. Методы, которые тесты не используют, падают с понятной ошибкой.
 * Журнал [calls] хранит тип, идентификатор и ключ идемпотентности каждого вызова.
 */
class FakeStorageApi : StorageApi {
    /** Журнал вызовов в порядке прихода: "тип:productId:ключ". */
    val calls = mutableListOf<String>()

    /** Ключи идемпотентности в порядке прихода. */
    val keys = mutableListOf<String>()

    var onPickProduct: (String, PickRequest) -> BaseResponse = { _, _ -> BaseResponse(success = true) }
    var onMove: (String, MoveProductRequest) -> BaseResponse = { _, _ -> BaseResponse(success = true) }
    var onPlace: (String, PlaceProductRequest) -> BaseResponse = { _, _ -> BaseResponse(success = true) }
    var onPickFromLocation: (PickFromLocationRequest) -> BaseResponse = { BaseResponse(success = true) }
    var onLocationItems: (String) -> LocationItemsResponse = { unused() }

    private fun record(kind: String, id: String, key: String) {
        calls += "$kind:$id:$key"
        keys += key
    }

    override suspend fun pickProduct(productId: String, request: PickRequest, idempotencyKey: String): BaseResponse {
        record("pick", productId, idempotencyKey)
        return onPickProduct(productId, request)
    }

    override suspend fun moveProduct(productId: String, request: MoveProductRequest, idempotencyKey: String): BaseResponse {
        record("move", productId, idempotencyKey)
        return onMove(productId, request)
    }

    override suspend fun placeProductToBuffer(productId: String, request: PlaceProductRequest, idempotencyKey: String): BaseResponse {
        record("place", productId, idempotencyKey)
        return onPlace(productId, request)
    }

    override suspend fun pickFromLocation(request: PickFromLocationRequest, idempotencyKey: String): BaseResponse {
        record("pickLoc", request.productId, idempotencyKey)
        return onPickFromLocation(request)
    }

    override suspend fun pickFromLocationBySkladId(request: PickFromLocationRequest, idempotencyKey: String): BaseResponse {
        record("pickLocSklad", request.productId, idempotencyKey)
        return onPickFromLocation(request)
    }

    override suspend fun getLocationItems(locationId: String, id_sklad: Int): LocationItemsResponse =
        onLocationItems(locationId)

    override suspend fun getLocationItems(locationId: String, id_sklad: String): LocationItemsResponse = unused()
    override suspend fun getEmployeeDetails(id: String): AuthResponse = unused()
    override suspend fun searchItem(shk: String?, article: String?): SearchResponse = unused()
    override suspend fun getUnits(productId: String): UnitResponse = unused()
    override suspend fun getProductDetails(shk: String?, article: String?): ProductSearchResponse = unused()
    override suspend fun getLocationProducts(locationId: String, skladId: Int): LocationProductsResponse = unused()
    override suspend fun getProductInfo(article: String?, shk: String?, skladId: Int?): ProductInfoResponse = unused()
    override suspend fun getInventoryItemByArticle(article: String, skladId: String): ArticleInventoryResponse = unused()
    override suspend fun confirmInventoryItem(request: InventoryRequest): BaseResponse = unused()
    override suspend fun getAllStorageItems(limit: Int, offset: Int, warehouseId: Int): StorageResponse = unused()
    override suspend fun getEmptyCells(skladId: Int): EmptyCellsResponse = unused()
    override suspend fun getLocationDetails(locationId: String, skladId: String?): LocationDetailsResponse = unused()
    override suspend fun searchProductByArticleOrBarcode(article: String?, shk: String?, idSklad: Int): Response<ProductInfoResponse> = unused()
    override suspend fun getAllStorageItems(): StorageResponse = unused()
    override suspend fun searchProductByArticleOrBarcode(query: String): StorageResponse = unused()

    private fun unused(): Nothing = throw UnsupportedOperationException("Не используется в тесте")
}
