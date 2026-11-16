package com.komus.sorage_mobile.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.komus.sorage_mobile.data.db.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Insert
    suspend fun insert(operation: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY id ASC")
    suspend fun getPending(): List<PendingOperationEntity>

    @Query("SELECT * FROM pending_operations WHERE status = 'FAILED' ORDER BY id ASC")
    fun getFailedFlow(): Flow<List<PendingOperationEntity>>

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE pending_operations SET status = 'FAILED', attempts = attempts + 1, lastError = :error WHERE id = :id")
    suspend fun markFailed(id: Long, error: String?)

    @Query("UPDATE pending_operations SET attempts = attempts + 1, lastError = :error WHERE id = :id")
    suspend fun markAttempt(id: Long, error: String?)

    @Query("UPDATE pending_operations SET idempotencyKey = :key WHERE id = :id AND idempotencyKey IS NULL")
    suspend fun assignKey(id: Long, key: String)

    @Query("UPDATE pending_operations SET status = 'PENDING', lastError = NULL WHERE id = :id")
    suspend fun resetToPending(id: Long)

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'PENDING'")
    fun pendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'FAILED'")
    fun failedCount(): Flow<Int>
}
