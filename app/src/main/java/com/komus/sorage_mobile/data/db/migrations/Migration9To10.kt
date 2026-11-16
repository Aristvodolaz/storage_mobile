package com.komus.sorage_mobile.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Ключ идемпотентности для отложенных операций. Существующие записи получают NULL и ключ создаётся при отправке. */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE pending_operations ADD COLUMN idempotencyKey TEXT")
    }
}
