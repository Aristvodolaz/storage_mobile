package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class LocationProductsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LocationProduct>,
    @SerializedName("message") val message: String? = null
)

data class LocationProduct(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shk") val shk: String,
    @SerializedName("article") val article: String,
    @SerializedName("idSklad") val idSklad: Int,
    @SerializedName("wrShk") val wrShk: String,
    @SerializedName("units") val units: List<Units>
)

data class Units(
    @SerializedName("prunitId") val prunitId: Int,
    @SerializedName("prunitName") val prunitName: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("productQnt") val productQnt: String,
    @SerializedName("conditionState") val conditionState: String,
    @SerializedName("expirationDate") val expirationDate: String
)