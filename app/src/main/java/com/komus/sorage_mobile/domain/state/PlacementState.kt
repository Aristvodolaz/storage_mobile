package com.komus.sorage_mobile.domain.state

sealed class PlacementState {
    object Idle : PlacementState()
    object Loading : PlacementState()
    data class Success(val message: String, val warnings: List<String> = emptyList()) : PlacementState()
    data class Error(val message: String) : PlacementState()
} 