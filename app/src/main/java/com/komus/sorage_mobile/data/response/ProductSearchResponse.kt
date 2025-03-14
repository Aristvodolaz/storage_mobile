package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class ProductSearchResponse(
    val success: Boolean,
    val data: List<ProductItem>
)

data class ProductItem(
    val id: String,
    val name: String,
    val article: String,
    val shk: String,
    val units: Map<String, UnitWithLocations>
)

data class UnitWithLocations(
    val id: String,
    val name: String,
    val locations: List<LocationDetails>
)

data class LocationDetails(
    val id: String,
    val wrShk: String,
    val name: String,
    val zone: String,
    val rack: String,
    val shelf: String,
    val position: String,
    val quantity: Int,
    val conditionState: String,
    val expirationDate: String,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String,
    val updatedBy: String
) 