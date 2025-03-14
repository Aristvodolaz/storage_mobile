package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.data.response.LocationItem

sealed class LocationItemsState {
    object Initial : LocationItemsState()
    object Loading : LocationItemsState()
    data class Success(val items: List<LocationItem>) : LocationItemsState()
    data class Error(val message: String) : LocationItemsState()
}

sealed class PickState {
    object Initial : PickState()
    object Loading : PickState()
    object Success : PickState()
    data class Error(val message: String) : PickState()
} 