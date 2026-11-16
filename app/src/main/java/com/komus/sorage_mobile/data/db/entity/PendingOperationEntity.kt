package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Операция, которую не удалось отправить из-за отсутствия связи.
 * Хранит тип операции, productId (если нужен для пути запроса) и тело запроса в JSON.
 */
@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val productId: String?,
    val body: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val status: String = STATUS_PENDING,
    val lastError: String? = null,
    // Ключ идемпотентности: один и тот же при всех повторах, чтобы сервер не выполнил операцию дважды
    val idempotencyKey: String? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_FAILED = "FAILED"
    }
}
