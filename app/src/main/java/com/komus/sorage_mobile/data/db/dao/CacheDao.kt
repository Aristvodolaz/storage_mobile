package com.komus.sorage_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.komus.sorage_mobile.data.db.entity.CacheEntity

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: CacheEntity)

    @Query("SELECT * FROM response_cache WHERE cacheKey = :key")
    suspend fun get(key: String): CacheEntity?
}
