package com.komus.sorage_mobile.data.response

data class SearchResponse(
    val success: Boolean,
    val data: List<SearchItem>
)
data class SearchItem(
    val ID: String,
    val NAME: String,
    val SHK: String,
    val ARTICLE_ID_REAL: String,
    val QNT_IN_PALLET: Int
)
