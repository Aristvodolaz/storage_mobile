package com.komus.sorage_mobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.komus.sorage_mobile.data.model.StorageItem

@Entity(tableName = "storage_items")
data class StorageItemEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val article: String,
    val shk: String,
    val productQnt: Int,
    val placeQnt: Int,
    val prunitId: Int,
    val prunitName: String,
    val wrShk: String,
    val idScklad: Int,
    val conditionState: String,
    val expirationDate: String,
    val createDate: String,
    val updateDate: String,
    val executor: String,
    val name_wr: String,
    val is_synced: Boolean = false
) {
    companion object {
        fun fromStorageItem(item: StorageItem) = StorageItemEntity(
            id = item.id,
            name = item.name,
            article = item.article,
            shk = item.shk,
            productQnt = item.productQnt,
            placeQnt = item.placeQnt,
            prunitId = item.prunitId,
            prunitName = item.prunitName,
            wrShk = item.wrShk,
            idScklad = item.idScklad,
            conditionState = item.conditionState,
            expirationDate = item.expirationDate,
            createDate = item.createDate,
            updateDate = item.updateDate,
            executor = item.executor,
            name_wr = item.name_wr
        )
    }
} 