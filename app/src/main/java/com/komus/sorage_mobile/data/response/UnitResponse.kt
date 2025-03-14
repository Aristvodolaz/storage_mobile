package com.komus.sorage_mobile.data.response

data class UnitResponse(
    val success: Boolean,
    val data: List<UnitItem>
)

data class UnitItem(
    val id: Int,
    val type: Int,
    val typeName: String,
    val brief: String,
    val parentQnt: Int,
    val productQnt: Int,
    val productId: String
)
