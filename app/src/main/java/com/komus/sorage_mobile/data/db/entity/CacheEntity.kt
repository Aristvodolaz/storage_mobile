package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Последний успешный ответ сервера, сохранённый в JSON для работы без сети. */
@Entity(tableName = "response_cache")
data class CacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val payload: String,
    val updatedAt: Long
)
