package com.komus.sorage_mobile.data.request

import com.google.gson.annotations.SerializedName

data class MoveProductRequest(
    @SerializedName("sourceLocationId") val sourceLocationId: String,
    @SerializedName("targetLocationId") val targetLocationId: String,
    @SerializedName("targetShk") val targetShk: String,
    @SerializedName("prunitId") val prunitId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("conditionState") val conditionState: String,
    @SerializedName("expirationDate") val expirationDate: String,
    @SerializedName("executor") val executor: String,
    @SerializedName("id_sklad") val skladId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("productQnt") val productQnt: String
    )