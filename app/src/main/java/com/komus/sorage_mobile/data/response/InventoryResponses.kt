package com.komus.sorage_mobile.data.response

import com.google.gson.annotations.SerializedName

data class InventoryItemResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("item")
    val item: InventoryItem?,
    @SerializedName("message")
    val message: String?
)

data class InventoryItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("article")
    val article: String,
    @SerializedName("shk")
    val barcode: String,
    @SerializedName("expected_quantity")
    val expectedQuantity: Int,
    @SerializedName("expected_location_id")
    val expectedLocationId: String,
    @SerializedName("expected_location_name")
    val expectedLocationName: String,
    @SerializedName("condition_state")
    val conditionState: String?,
    @SerializedName("expiration_date")
    val expirationDate: String?
)

data class LocationInventoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("items")
    val items: List<InventoryItem>,
    @SerializedName("message")
    val message: String?
)

data class UpdateInventoryRequest(
    @SerializedName("item_id")
    val itemId: String,
    @SerializedName("location_id")
    val locationId: String,
    @SerializedName("actual_quantity")
    val actualQuantity: Int,
    @SerializedName("sklad_id")
    val skladId: String?,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("notes")
    val notes: String?
)

data class SyncInventoryRequest(
    @SerializedName("items")
    val items: List<UpdateInventoryRequest>,
    @SerializedName("sklad_id")
    val skladId: String?,
    @SerializedName("user_name")
    val userName: String
)

data class InventoryVerificationResult(
    val isFoundInExpectedLocation: Boolean,
    val isFoundInDifferentLocation: Boolean,
    val isNotFound: Boolean,
    val quantityMatches: Boolean,
    val message: String
) 