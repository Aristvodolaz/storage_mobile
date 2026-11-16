package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.repository.PickRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/** Сквозная проверка: репозиторий пика без сети кладёт операцию в очередь и отвечает «сохранено». */
class PickRepositoryOfflineTest {

    @Test
    fun pickProduct_withoutNetwork_isQueuedAndReportedAsQueued() = runBlocking {
        val dao = FakePendingOperationDao()
        val api = FakeStorageApi().apply { onPickProduct = { _, _ -> throw IOException("нет сети") } }
        val scheduler = FakeDrainScheduler()
        val repo = PickRepository(api, OfflineQueue(api, dao, scheduler), ResponseCache(FakeCacheDao()))

        val response = repo.pickProduct(
            productId = "P1", locationId = "L1", prunitId = "10", quantity = 3, executor = "user"
        )

        assertTrue(OfflineQueue.isQueued(response))
        assertEquals(1, dao.rows.size)
        assertEquals("PICK_PRODUCT", dao.rows[0].type)
        assertEquals(PendingOperationEntity.STATUS_PENDING, dao.rows[0].status)
        assertEquals(1, scheduler.requests)
    }

    @Test
    fun pickProduct_online_isNotQueued() = runBlocking {
        val dao = FakePendingOperationDao()
        val api = FakeStorageApi()
        val repo = PickRepository(api, OfflineQueue(api, dao, FakeDrainScheduler()), ResponseCache(FakeCacheDao()))

        val response = repo.pickProduct(
            productId = "P1", locationId = "L1", prunitId = "10", quantity = 3, executor = "user"
        )

        assertTrue(response.success)
        assertFalse(OfflineQueue.isQueued(response))
        assertTrue(dao.rows.isEmpty())
    }
}
