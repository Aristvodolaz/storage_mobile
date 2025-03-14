package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.data.response.SearchItem

sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val data: List<SearchItem>) : SearchState()
    data class Error(val message: String) : SearchState()
}
