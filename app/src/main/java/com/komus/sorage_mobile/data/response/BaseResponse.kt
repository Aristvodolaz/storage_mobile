package com.komus.sorage_mobile.data.response

data class BaseResponse(
    val success: Boolean,
    val message: String? = null,
    val errorCode: Int? = null,
    val warnings: List<Map<String, String>>? = null
) 