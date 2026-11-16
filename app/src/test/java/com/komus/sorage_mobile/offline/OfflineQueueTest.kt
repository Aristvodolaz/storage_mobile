package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.offline.OfflineOpType
import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class OfflineQueueTest {

    private lateinit var dao: FakePendingOperationDao
    private lateinit var api: FakeStorageApi
    private lateinit var scheduler: FakeDrainScheduler
    private lateinit var queue: OfflineQueue

    private val pickBody = PickRequest(locationId = "L1", prunitId = "10", quantity = 2, executor = "user")

    @Before
    fun setUp() {
        dao = FakePendingOperationDao()
        api = FakeStorageApi()
        scheduler = FakeDrainScheduler()
        queue = OfflineQueue(api, dao, scheduler)
    }

    private fun httpError(code: Int): HttpException =
        HttpException(Response.error<Any>(code, "error".toResponseBody("text/plain".toMediaType())))

    @Test
    fun sendOrQueue_withoutNetwork_storesOperationAndRequestsDrain() = runBlocking {
        val response = queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) {
            throw IOException("нет сети")
        }

        assertTrue(OfflineQueue.isQueued(response))
        assertEquals(1, dao.rows.size)
        assertEquals("PICK_PRODUCT", dao.rows[0].type)
        assertEquals("P1", dao.rows[0].productId)
        assertEquals(PendingOperationEntity.STATUS_PENDING, dao.rows[0].status)
        assertEquals(1, scheduler.requests)
    }

    @Test
    fun sendOrQueue_onSuccess_doesNotQueue() = runBlocking {
        val response = queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) {
            BaseResponse(success = true, message = "ok")
        }

        assertFalse(OfflineQueue.isQueued(response))
        assertTrue(dao.rows.isEmpty())
        assertEquals(0, scheduler.requests)
    }

    @Test
    fun sendOrQueue_onServerRejection_returnsErrorAndDoesNotQueue() = runBlocking {
        val response = queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) {
            BaseResponse(success = false, message = "Недостаточно товара")
        }

        assertFalse(response.success)
        assertEquals("Недостаточно товара", response.message)
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun drain_sendsOperationsInOrderAndDeletesThem() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P2", pickBody) { throw IOException() }

        val finished = queue.drain()

        assertTrue(finished)
        assertEquals(listOf("pick:P1", "pick:P2"), api.calls.map { it.substringBeforeLast(':') })
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun drain_stopsOnNetworkError_andKeepsOperationPending() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P2", pickBody) { throw IOException() }
        api.onPickProduct = { _, _ -> throw IOException("связь пропала") }

        val finished = queue.drain()

        assertFalse(finished)
        assertEquals(listOf("pick:P1"), api.calls.map { it.substringBeforeLast(':') })
        assertEquals(2, dao.rows.size)
        assertEquals(1, dao.rows[0].attempts)
    }

    @Test
    fun drain_rejectedByServer_marksFailedAndContinues() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P2", pickBody) { throw IOException() }
        api.onPickProduct = { productId, _ ->
            if (productId == "P1") BaseResponse(success = false, message = "Товар не найден")
            else BaseResponse(success = true)
        }

        val finished = queue.drain()

        assertTrue(finished)
        assertEquals(listOf("pick:P1", "pick:P2"), api.calls.map { it.substringBeforeLast(':') })
        assertEquals(1, dao.rows.size)
        assertEquals(PendingOperationEntity.STATUS_FAILED, dao.rows[0].status)
        assertEquals("Товар не найден", dao.rows[0].lastError)
    }

    @Test
    fun drain_serverError5xx_keepsOperationAndStops() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P2", pickBody) { throw IOException() }
        api.onPickProduct = { _, _ -> throw httpError(503) }

        val finished = queue.drain()

        assertFalse(finished)
        assertEquals(listOf("pick:P1"), api.calls.map { it.substringBeforeLast(':') })
        assertEquals(2, dao.rows.size)
        assertTrue(dao.rows.all { it.status == PendingOperationEntity.STATUS_PENDING })
    }

    @Test
    fun drain_clientError4xx_marksFailed() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        api.onPickProduct = { _, _ -> throw httpError(400) }

        assertTrue(queue.drain())

        assertEquals(PendingOperationEntity.STATUS_FAILED, dao.rows[0].status)
        assertEquals("HTTP 400", dao.rows[0].lastError)
    }

    @Test
    fun retry_returnsFailedOperationToPendingAndRequestsDrain() = runBlocking {
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { throw IOException() }
        api.onPickProduct = { _, _ -> BaseResponse(success = false, message = "ошибка") }
        queue.drain()
        val id = dao.rows[0].id
        val requestsBefore = scheduler.requests

        queue.retry(id)

        assertEquals(PendingOperationEntity.STATUS_PENDING, dao.rows[0].status)
        assertEquals(requestsBefore + 1, scheduler.requests)
    }

    @Test
    fun sendOrQueue_storesTheSameKeyThatWasSent() = runBlocking {
        var sentKey: String? = null
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { key ->
            sentKey = key
            throw IOException("нет сети")
        }

        assertTrue(sentKey!!.isNotBlank())
        assertEquals(sentKey, dao.rows[0].idempotencyKey)
    }

    @Test
    fun retry_afterLostResponse_reusesTheSameKey() = runBlocking {
        // Первая отправка дошла до сервера, но ответ потерян: операция уходит в очередь с тем же ключом
        var firstKey: String? = null
        queue.sendOrQueue(OfflineOpType.PICK_PRODUCT, "P1", pickBody) { key ->
            firstKey = key
            throw IOException("ответ потерян")
        }

        assertTrue(queue.drain())

        assertEquals(listOf(firstKey), api.keys)
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun drain_legacyOperationWithoutKey_getsOneKeyAcrossRetries() = runBlocking {
        dao.insert(
            PendingOperationEntity(
                type = "MOVE",
                productId = "P1",
                body = com.google.gson.Gson().toJson(pickBody),
                createdAt = 0L,
                idempotencyKey = null
            )
        )
        api.onMove = { _, _ -> throw IOException("нет сети") }
        assertFalse(queue.drain())
        val keyAfterFirstAttempt = dao.rows[0].idempotencyKey
        assertTrue(keyAfterFirstAttempt != null)

        api.onMove = { _, _ -> BaseResponse(success = true) }
        assertTrue(queue.drain())

        assertEquals(listOf(keyAfterFirstAttempt, keyAfterFirstAttempt), api.keys)
    }

    @Test
    fun discard_removesOperation() = runBlocking {
        queue.sendOrQueue(OfflineOpType.MOVE, "P1", pickBody) { throw IOException() }

        queue.discard(dao.rows[0].id)

        assertTrue(dao.rows.isEmpty())
    }
}
