package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class ProductInfoResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ProductInfo?,
    @SerializedName("message") val message: String? = null
)

data class ProductInfo(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("shk") val shk: String,
    @SerializedName("article") val article: String,
    @SerializedName("totalQuantity") val totalQuantity: Int,
    @SerializedName("items") val items: List<ProductLocation>
)

data class ProductLocation(
    @SerializedName("id") val id: Int,
    @SerializedName("locationId") val locationId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("conditionState") val conditionState: String?,
    @SerializedName("expirationDate") val expirationDate: String?
) 