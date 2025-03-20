package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "placements")
data class PlacementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val prunitId: String,
    val quantity: Int,
    val conditionState: String,
    val expirationDate: String,
    val executor: String,
    val wrShk: String?,
    val name: String?,
    val shk: String?,
    val article: String?,
    val skladId: String?,
    val reason: String?,
    val productQnt: Int,
    val is_synced: Boolean = false
) 