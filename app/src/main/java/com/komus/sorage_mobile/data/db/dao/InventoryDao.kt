package com.komus.sorage_mobile.data.db.dao

import androidx.room.*
import com.komus.sorage_mobile.data.db.entity.InventoryEntity
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import com.komus.sorage_mobile.data.db.entity.StorageItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory WHERE cell_id = :locationId")
    fun getItemsByLocationId(locationId: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE article = :article")
    fun getItemsByArticle(article: String): Flow<List<InventoryEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity)

    @Update
    suspend fun updateItem(item: InventoryEntity): Int

    @Query("UPDATE inventory SET isSynced = 1 WHERE id = :itemId")
    suspend fun markAsSynced(itemId: Long)

    @Query("DELETE FROM inventory WHERE cell_id = :locationId")
    suspend fun deleteItemsByLocationId(locationId: String): Int

    @Query("DELETE FROM inventory WHERE isSynced = 1")
    suspend fun deleteSyncedItems(): Int

    // Методы для работы с товарами
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorageItems(items: List<StorageItemEntity>)

    @Query("SELECT * FROM storage_items WHERE article = :article OR shk = :barcode")
    suspend fun findStorageItemByArticleOrBarcode(article: String, barcode: String): StorageItemEntity?

    @Query("SELECT * FROM storage_items WHERE idScklad = :warehouseId")
    fun getStorageItemsByWarehouse(warehouseId: Int): Flow<List<StorageItemEntity>>

    @Query("DELETE FROM storage_items")
    suspend fun clearStorageItems()
    @Query("SELECT * FROM inventory WHERE isSynced = 0")
    fun getUnsyncedItemsFlow(): Flow<List<InventoryEntity>>

    // Методы для работы с оффлайн размещениями
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePlacement(placement: OfflinePlacementEntity)

    @Query("SELECT * FROM offline_placements WHERE isSynced = 0")
    fun getUnsyncedPlacements(): Flow<List<OfflinePlacementEntity>>

    @Query("UPDATE offline_placements SET isSynced = 1 WHERE id = :placementId")
    suspend fun markPlacementAsSynced(placementId: String)

    @Query("DELETE FROM offline_placements WHERE isSynced = 1")
    suspend fun deleteSyncedPlacements()

    @Query("SELECT * FROM inventory WHERE isSynced = 0")
    suspend fun getUnsyncedItems(): List<InventoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryEntity>)

    @Query("SELECT * FROM inventory WHERE cell_id = :cellId")
    fun getInventoryByCellId(cellId: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE isSynced = 0")
    suspend fun getUnsyncedInventoryItems(): List<InventoryEntity>

    @Query("UPDATE inventory SET isSynced = 1 WHERE id = :id")
    suspend fun markInventoryAsSynced(id: String)

    @Query("SELECT COUNT(*) FROM inventory WHERE isSynced = 0")
    fun getUnsyncedInventoryCount(): Flow<Int>
}
