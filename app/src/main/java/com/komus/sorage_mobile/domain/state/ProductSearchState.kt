package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.data.response.ProductItem

sealed class ProductSearchState {
    object Initial : ProductSearchState()
    object Loading : ProductSearchState()
    data class Success(val products: List<ProductItem>) : ProductSearchState()
    data class Error(val message: String) : ProductSearchState()
} 