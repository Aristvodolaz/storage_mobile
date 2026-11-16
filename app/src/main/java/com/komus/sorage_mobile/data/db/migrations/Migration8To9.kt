package com.komus.sorage_mobile.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Очередь отложенных операций и кэш ответов для работы без сети. Данные существующих таблиц не трогаем. */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pending_operations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                productId TEXT,
                body TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                attempts INTEGER NOT NULL,
                status TEXT NOT NULL,
                lastError TEXT
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS response_cache (
                cacheKey TEXT PRIMARY KEY NOT NULL,
                payload TEXT NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
