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
    @SerializedName("locationId") val locationId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("conditionState") val conditionState: String?,
    @SerializedName("expirationDate") val expirationDate: String?
) 