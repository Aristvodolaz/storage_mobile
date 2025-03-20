package com.komus.sorage_mobile.data.db.dao

import androidx.room.*
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflinePlacementDao {
    @Query("SELECT * FROM offline_placements WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedPlacements(): Flow<List<OfflinePlacementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacement(placement: OfflinePlacementEntity)

    @Query("UPDATE offline_placements SET isSynced = 1 WHERE id = :placementId")
    suspend fun markAsSynced(placementId: String)

    @Query("DELETE FROM offline_placements WHERE isSynced = 1")
    suspend fun deleteSyncedPlacements()

    @Query("SELECT * FROM offline_placements WHERE id = :id")
    suspend fun getPlacementById(id: String): OfflinePlacementEntity?

    @Query("UPDATE offline_placements SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp, errorMessage = :error WHERE id = :placementId")
    suspend fun updateSyncAttempt(placementId: String, timestamp: Long, error: String?)

    @Query("SELECT COUNT(*) FROM offline_placements WHERE isSynced = 0")
    fun getUnsyncedPlacementsCount(): Flow<Int>

    @Query("SELECT * FROM offline_placements WHERE isSynced = 0 AND syncAttempts < 3 AND (lastSyncAttempt IS NULL OR ((:currentTime - lastSyncAttempt) > :retryInterval))")
    suspend fun getPlacementsForSync(currentTime: Long, retryInterval: Long = 300000): List<OfflinePlacementEntity>
} 