package com.komus.sorage_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.komus.sorage_mobile.data.db.entity.StorageItemEntity

@Dao
interface StorageDao {
    @Query("SELECT * FROM storage_items WHERE shk = :shk")
    suspend fun findItemsByShk(shk: String): List<StorageItemEntity>

    @Query("SELECT * FROM storage_items WHERE article = :article")
    suspend fun findItemsByArticle(article: String): List<StorageItemEntity>
}
