package com.komus.sorage_mobile.data.request

data class PickRequest(
    val locationId: String,
    val prunitId: String,
    val quantity: Int,
    val executor: String
) 