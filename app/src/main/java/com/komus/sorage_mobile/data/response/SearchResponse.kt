package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<SearchItem>? = null,
    @SerializedName("msg") val msg: String? = null,
    @SerializedName("errorCode") val errorCode: Int? = null
)
data class SearchItem(
    @SerializedName("id") val ID: String,
    @SerializedName("name") val NAME: String,
    @SerializedName("shk") val SHK: String? = null,
    @SerializedName("article_id_real") val ARTICLE_ID_REAL: String,
    @SerializedName("qnt_in_pallet") val QNT_IN_PALLET: Int,
    @SerializedName("CONDITION_STATE") val CONDITION_STATE: String? = null,
    @SerializedName("REASON") val REASON: String? = null
)
