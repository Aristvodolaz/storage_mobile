package com.komus.sorage_mobile.data.response

data class LocationItemsResponse(
    val success: Boolean,
    val data: List<LocationItem>,
    val message: String? = null,
    val errorCode: Int? = null
)

data class Units(
    val prunitId: Int,
    val prunitName: String,
    val quantity: String,
    val conditionState: String,
    val expirationDate: String? = null
)

data class LocationItem(
    val id: String,
    val name: String,
    val article: String,
    val shk: String,
    val locationId: Int? = null,
    val units: List<Units> = emptyList(),
    
    // Поля для обратной совместимости
    val productId: String? = null,
    val prunitId: String? = null,
    val quantity: Int? = null,
    val barcode: String? = null,
    val idSklad: Int? = null,
    val wrShk: String? = null
) 