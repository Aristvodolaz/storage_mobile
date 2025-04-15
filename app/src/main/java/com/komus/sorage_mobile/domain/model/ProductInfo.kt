package com.komus.sorage_mobile.domain.model

data class ProductInfoResponse(
    val success: Boolean,
    val data: ProductInfoData
)

data class ProductInfoData(
    val article: String,
    val shk: String,
    val name: String,
    val totalItems: Int,
    val totalQuantity: Int,
    val items: List<ProductItem>,
    val locations: List<LocationInfo>
)

data class ProductItem(
    val id: Long,
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
    val executor: String,
    val name_wr_shk: String,
    val reason: String
)

data class LocationInfo(
    val locationId: String,
    val idScklad: Int,
    val items: List<ProductItem>
) 