package com.komus.sorage_mobile.offline

import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.response.BaseResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ResponseCacheTest {

    private lateinit var dao: FakeCacheDao
    private lateinit var cache: ResponseCache

    @Before
    fun setUp() {
        dao = FakeCacheDao()
        cache = ResponseCache(dao)
    }

    @Test
    fun fetch_online_returnsServerValueAndStoresIt() = runBlocking {
        val value = cache.fetch("key", BaseResponse::class.java, { it.success }) {
            BaseResponse(success = true, message = "свежие данные")
        }

        assertEquals("свежие данные", value.message)
        assertTrue(dao.entries.containsKey("key"))
    }

    @Test
    fun fetch_offline_returnsLastCachedValue() = runBlocking {
        cache.fetch("key", BaseResponse::class.java, { it.success }) {
            BaseResponse(success = true, message = "сохранено")
        }

        val value = cache.fetch("key", BaseResponse::class.java, { it.success }) {
            throw IOException("нет сети")
        }

        assertEquals("сохранено", value.message)
    }

    @Test
    fun fetch_offlineWithoutCache_rethrowsNetworkError() = runBlocking {
        try {
            cache.fetch("missing", BaseResponse::class.java) { throw IOException("нет сети") }
            fail("ожидалась ошибка сети")
        } catch (e: IOException) {
            assertEquals("нет сети", e.message)
        }
    }

    @Test
    fun fetch_doesNotCacheUnsuccessfulResponse() = runBlocking {
        cache.fetch("key", BaseResponse::class.java, { it.success }) {
            BaseResponse(success = false, message = "ошибка")
        }

        assertTrue(dao.entries.isEmpty())
    }
}
