package com.komus.sorage_mobile.data.response

// Ответ от API
data class EmptyCellsResponse(
    val success: Boolean,
    val data: EmptyCellsData,
    val message: String? = null
)

data class EmptyCellsData(
    val cells: List<EmptyCell>,
    val count: Int
)

data class EmptyCell(
    val id: String,
    val name: String,
    val shk: String,
    val wrHouse: String
)
