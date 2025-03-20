package com.komus.sorage_mobile.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем таблицу offline_placements
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_placements (
                id TEXT PRIMARY KEY NOT NULL,
                article TEXT,
                barcode TEXT,
                prunitTypeId INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                isGoodCondition INTEGER NOT NULL,
                reason TEXT,
                cellBarcode TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем таблицу storage_items
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS storage_items (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                article TEXT NOT NULL,
                shk TEXT NOT NULL,
                productQnt INTEGER NOT NULL,
                placeQnt INTEGER NOT NULL,
                prunitId INTEGER NOT NULL,
                prunitName TEXT NOT NULL,
                wrShk TEXT NOT NULL,
                idScklad INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                createDate TEXT NOT NULL,
                updateDate TEXT NOT NULL,
                executor TEXT NOT NULL
            )
        """)

        // Создаем таблицу offline_placements с обновленной структурой
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_placements (
                id TEXT PRIMARY KEY NOT NULL,
                barcode TEXT NOT NULL,
                article TEXT NOT NULL,
                productQnt INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                cellBarcode TEXT NOT NULL,
                packageType TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isSynced INTEGER NOT NULL DEFAULT 0,
                reason TEXT NOT NULL,
                condition TEXT NOT NULL,
                endDate TEXT NOT NULL,
                prunitTypeId INTEGER NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                errorMessage TEXT,
                syncAttempts INTEGER NOT NULL DEFAULT 0,
                lastSyncAttempt INTEGER
            )
        """)
    }
}

val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем таблицу offline_placements
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_placements (
                id TEXT PRIMARY KEY NOT NULL,
                article TEXT,
                barcode TEXT,
                prunitTypeId INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                isGoodCondition INTEGER NOT NULL,
                reason TEXT,
                cellBarcode TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """)

        // Создаем таблицу storage_items
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS storage_items (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                article TEXT NOT NULL,
                shk TEXT NOT NULL,
                productQnt INTEGER NOT NULL,
                placeQnt INTEGER NOT NULL,
                prunitId INTEGER NOT NULL,
                prunitName TEXT NOT NULL,
                wrShk TEXT NOT NULL,
                idScklad INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                createDate TEXT NOT NULL,
                updateDate TEXT NOT NULL,
                executor TEXT NOT NULL
            )
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем таблицу placements
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS placements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId TEXT NOT NULL,
                prunitId TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                executor TEXT NOT NULL,
                wrShk TEXT,
                name TEXT,
                shk TEXT,
                article TEXT,
                skladId TEXT,
                reason TEXT,
                productQnt INTEGER NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_1_4 = object : Migration(1, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем все таблицы для прямой миграции с версии 1 на 4
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_placements (
                id TEXT PRIMARY KEY NOT NULL,
                article TEXT,
                barcode TEXT,
                prunitTypeId INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                isGoodCondition INTEGER NOT NULL,
                reason TEXT,
                cellBarcode TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS storage_items (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                article TEXT NOT NULL,
                shk TEXT NOT NULL,
                productQnt INTEGER NOT NULL,
                placeQnt INTEGER NOT NULL,
                prunitId INTEGER NOT NULL,
                prunitName TEXT NOT NULL,
                wrShk TEXT NOT NULL,
                idScklad INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                createDate TEXT NOT NULL,
                updateDate TEXT NOT NULL,
                executor TEXT NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS placements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId TEXT NOT NULL,
                prunitId TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                executor TEXT NOT NULL,
                wrShk TEXT,
                name TEXT,
                shk TEXT,
                article TEXT,
                skladId TEXT,
                reason TEXT,
                productQnt INTEGER NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Пересоздаем таблицу inventory с правильной структурой
        database.execSQL("DROP TABLE IF EXISTS inventory")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS inventory (
                id INTEGER PRIMARY KEY NOT NULL,
                cell_id TEXT NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                condition TEXT NOT NULL,
                reason TEXT NOT NULL,
                article TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_1_5 = object : Migration(1, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Создаем все таблицы для прямой миграции с версии 1 на 5
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_placements (
                id TEXT PRIMARY KEY NOT NULL,
                article TEXT,
                barcode TEXT,
                prunitTypeId INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                startDate TEXT NOT NULL,
                endDate TEXT NOT NULL,
                isGoodCondition INTEGER NOT NULL,
                reason TEXT,
                cellBarcode TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                isSynced INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS storage_items (
                id INTEGER PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                article TEXT NOT NULL,
                shk TEXT NOT NULL,
                productQnt INTEGER NOT NULL,
                placeQnt INTEGER NOT NULL,
                prunitId INTEGER NOT NULL,
                prunitName TEXT NOT NULL,
                wrShk TEXT NOT NULL,
                idScklad INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                createDate TEXT NOT NULL,
                updateDate TEXT NOT NULL,
                executor TEXT NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS placements (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId TEXT NOT NULL,
                prunitId TEXT NOT NULL,
                quantity INTEGER NOT NULL,
                conditionState TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                executor TEXT NOT NULL,
                wrShk TEXT,
                name TEXT,
                shk TEXT,
                article TEXT,
                skladId TEXT,
                reason TEXT,
                productQnt INTEGER NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS inventory (
                id INTEGER PRIMARY KEY NOT NULL,
                cell_id TEXT NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                condition TEXT NOT NULL,
                reason TEXT NOT NULL,
                article TEXT NOT NULL,
                expirationDate TEXT NOT NULL,
                is_synced INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
} 