package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.komus.sorage_mobile.domain.model.InventoryItem
import java.util.UUID

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val barcode: String,
    val article: String,
    val name: String,
    val quantity: Int,
    val cellBarcode: String,
    @ColumnInfo(name = "cell_id")
    val cellId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val errorMessage: String? = null,
    val syncAttempts: Int = 0,
    val product_id: Long,
    val condition: String,
    val reason: String,
    val expirationDate: String
) {
    fun toInventoryItem(): InventoryItem {
        return InventoryItem(
            id = id,
            name = name,
            article = article,
            barcode = barcode,
            locationId = cellId,
            locationName = "",
            expectedQuantity = quantity,
            actualQuantity = quantity,
            expirationDate = expirationDate,
            condition = condition,
            reason = reason,
            isChecked = false
        )
    }

    companion object {
        fun fromInventoryItem(item: InventoryItem, executor: String, isSynced: Boolean = false): InventoryEntity {
            return InventoryEntity(
                id = item.id,
                barcode = item.barcode,
                article = item.article,
                name = item.name,
                quantity = item.expectedQuantity,
                cellBarcode = "",
                cellId = item.locationId,
                product_id = 0,
                condition = item.condition,
                reason = item.reason ?: "",
                expirationDate = item.expirationDate,
                isSynced = isSynced
            )
        }
    }
} 