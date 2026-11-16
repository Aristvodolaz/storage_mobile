package com.komus.sorage_mobile.data.offline

import com.google.gson.Gson
import com.komus.sorage_mobile.data.db.dao.CacheDao
import com.komus.sorage_mobile.data.db.entity.CacheEntity
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Читает с сервера и запоминает успешный ответ. Без связи (IOException) отдаёт последний сохранённый.
 * Если сохранённого нет, ошибка пробрасывается дальше.
 */
@Singleton
class ResponseCache @Inject constructor(
    private val dao: CacheDao
) {
    private val gson = Gson()

    suspend fun <T : Any> fetch(
        key: String,
        clazz: Class<T>,
        isCacheable: (T) -> Boolean = { true },
        block: suspend () -> T
    ): T {
        return try {
            val value = block()
            if (isCacheable(value)) {
                dao.put(CacheEntity(key, gson.toJson(value), System.currentTimeMillis()))
            }
            value
        } catch (e: IOException) {
            val cached = dao.get(key) ?: throw e
            gson.fromJson(cached.payload, clazz)
        }
    }
}
