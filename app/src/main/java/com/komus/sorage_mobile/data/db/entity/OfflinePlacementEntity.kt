package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "offline_placements")
data class OfflinePlacementEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val barcode: String,
    val article: String,
    val productQnt: Int,
    val quantity: Int,
    val cellBarcode: String,
    val packageType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val reason: String,
    val condition: String,
    val endDate: String,
    val prunitTypeId: Int,
    val name: String = "",
    val errorMessage: String? = null,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null
) 