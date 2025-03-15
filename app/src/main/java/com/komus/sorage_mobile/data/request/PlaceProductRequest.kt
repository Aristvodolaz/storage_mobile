package com.komus.sorage_mobile.data.request

import com.google.gson.annotations.SerializedName

data class PlaceProductRequest(
    @SerializedName("prunitId") val prunitId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("conditionState") val conditionState: String,
    @SerializedName("expirationDate") val expirationDate: String,
    @SerializedName("executor") val executor: String,
    @SerializedName("wrShk") val wrShk: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("shk") val shk: String? = null,
    @SerializedName("article") val article: String? = null,
    @SerializedName("sklad_id") val skladId: String? = null
) 