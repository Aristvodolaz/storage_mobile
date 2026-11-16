package com.komus.sorage_mobile.data.response

data class LocationDetailsResponse(
    val success: Boolean,
    val data: LocationDetailsData? = null,
    val message: String? = null
)

data class LocationDetailsData(
    val locationId: String,
    val locationName: String? = null
)
