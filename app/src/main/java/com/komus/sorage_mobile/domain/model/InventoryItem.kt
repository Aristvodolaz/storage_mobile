package com.komus.sorage_mobile.domain.model

/**
 * Модель данных товара для инвентаризации
 */
data class InventoryItem(
    val id: String,
    val name: String,
    val article: String,
    val barcode: String,
    val locationId: String,
    val locationName: String,
    val expectedQuantity: Int,
    val actualQuantity: Int = expectedQuantity,
    val isChecked: Boolean = false
) 