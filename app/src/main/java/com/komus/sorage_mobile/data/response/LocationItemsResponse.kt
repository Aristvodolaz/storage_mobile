package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class LocationItemsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LocationItem>,
    @SerializedName("message") val message: String? = null,
    @SerializedName("errorCode") val errorCode: Int? = null
)

data class ItemUnit(
    @SerializedName("prunitId") val prunitId: Int,
    @SerializedName("prunitName") val prunitName: String,
    @SerializedName("quantity") val quantity: String,
    @SerializedName("conditionState") val conditionState: String,
    @SerializedName("expirationDate") val expirationDate: String? = null,
    @SerializedName("productQnt") val productQnt: String,
    @SerializedName("name_wr_shk") val name_wr_shk: String,
    @SerializedName("reason") val reason: String? = null
)

data class LocationItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("article") val article: String,
    @SerializedName("shk") val shk: String,
    @SerializedName("idSklad") val idSklad: Int,
    @SerializedName("wrShk") val wrShk: String,
    @SerializedName("units") val units: List<ItemUnit> = emptyList()
)

fun ItemUnit.placeQuantity(): Double = quantity.toDoubleOrNull() ?: 0.0

// Pieces in one EX from Product_QNT. 0/empty means 1 piece per EX.
fun ItemUnit.exNesting(): Int {
    return productQnt.toDoubleOrNull()?.takeIf { it > 0 }?.toInt() ?: 1
}

fun LocationItem.availableExQuantity(): Int {
    val unit = units.firstOrNull() ?: return 0
    return kotlin.math.floor(unit.placeQuantity() / unit.exNesting()).toInt()
}
