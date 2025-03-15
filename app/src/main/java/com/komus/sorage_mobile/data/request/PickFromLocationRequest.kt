package com.komus.sorage_mobile.data.request

data class PickFromLocationRequest(
    val productId: String,
    val WR_SHK: String,
    val prunitId: String,
    val quantity: Int,
    val executor: String,
    val sklad_id: String
) 