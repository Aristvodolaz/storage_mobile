package com.komus.sorage_mobile.domain.state

sealed class MoveProductState {
    object Initial : MoveProductState()
    object Loading : MoveProductState()
    object Success : MoveProductState()
    data class Error(val message: String) : MoveProductState()
} 