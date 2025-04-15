package com.komus.sorage_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.komus.sorage_mobile.data.db.entity.PlacementEntity

@Dao
interface PlacementDao {
    @Query("SELECT * FROM placements WHERE is_synced = 0")
    suspend fun getUnsyncedPlacements(): List<PlacementEntity>

    @Query("UPDATE placements SET is_synced = 1 WHERE id = :placementId")
    suspend fun markAsSynced(placementId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacement(placement: PlacementEntity): Long
} 