package com.komus.sorage_mobile.data.response

data class LocationItemsResponse(
    val success: Boolean,
    val data: List<LocationItem>,
    val message: String? = null,
    val errorCode: Int? = null
)

data class LocationItem(
    val productId: String,
    val prunitId: String,
    val name: String,
    val quantity: Int,
    val article: String,
    val barcode: String? = null
) 