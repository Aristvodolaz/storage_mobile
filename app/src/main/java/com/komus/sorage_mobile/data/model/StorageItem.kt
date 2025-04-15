package com.komus.sorage_mobile.data.model

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class StorageResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: StorageData
)

data class StorageData(
    @SerializedName("items")
    val items: List<StorageItem>
)

data class StorageItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("article")
    val article: String,
    @SerializedName("shk")
    val shk: String,
    @SerializedName("productQnt")
    val productQnt: Int,
    @SerializedName("placeQnt")
    val placeQnt: Int,
    @SerializedName("prunitId")
    val prunitId: Int,
    @SerializedName("prunitName")
    val prunitName: String,
    @SerializedName("wrShk")
    val wrShk: String,
    @SerializedName("idScklad")
    val idScklad: Int,
    @SerializedName("conditionState")
    val conditionState: String,
    @SerializedName("expirationDate")
    val expirationDate: String,
    @SerializedName("createDate")
    val createDate: String,
    @SerializedName("updateDate")
    val updateDate: String,
    @SerializedName("executor")
    val executor: String,
    @SerializedName("name_wr_shk")
    val name_wr: String,
) 