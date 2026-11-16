package com.komus.sorage_mobile.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.komus.sorage_mobile.data.db.dao.CacheDao
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.dao.OfflinePlacementDao
import com.komus.sorage_mobile.data.db.dao.PendingOperationDao
import com.komus.sorage_mobile.data.db.dao.PlacementDao
import com.komus.sorage_mobile.data.db.dao.StorageDao
import com.komus.sorage_mobile.data.db.entity.CacheEntity
import com.komus.sorage_mobile.data.db.entity.InventoryEntity
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import com.komus.sorage_mobile.data.db.entity.PlacementEntity
import com.komus.sorage_mobile.data.db.entity.StorageItemEntity

@Database(
    entities = [
        InventoryEntity::class,
        OfflinePlacementEntity::class,
        StorageItemEntity::class,
        PlacementEntity::class,
        PendingOperationEntity::class,
        CacheEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun offlinePlacementDao(): OfflinePlacementDao
    abstract fun placementDao(): PlacementDao
    abstract fun storageDao(): StorageDao
    abstract fun pendingOperationDao(): PendingOperationDao
    abstract fun cacheDao(): CacheDao
}
