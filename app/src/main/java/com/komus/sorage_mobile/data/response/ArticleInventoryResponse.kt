package com.komus.sorage_mobile.data.response
data class ArticleInventoryResponse(
    val success: Boolean,
    val data: ProductData
)

data class ProductData(
    val article: String,
    val shk: String,
    val name: String,
    val totalItems: Int,
    val totalQuantity: Int,
    val items: List<ArticletItem>,
    val locations: List<Location>
)

data class ArticletItem(
    val id: Int,
    val name: String,
    val article: String,
    val shk: String,
    val productQnt: Int,
    val placeQnt: Int,
    val prunitId: Int,
    val prunitName: String,
    val wrShk: String,
    val idScklad: Int,
    val conditionState: String,
    val expirationDate: String,
    val createDate: String,
    val updateDate: String,
    val executor: String
)

data class Location(
    val locationId: String,
    val idScklad: Int,
    val items: List<ArticletItem>
)
