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
    @SerializedName("productQnt") val productQnt: String
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