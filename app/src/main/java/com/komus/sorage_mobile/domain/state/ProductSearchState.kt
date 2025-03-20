package com.komus.sorage_mobile.domain.state

import com.komus.sorage_mobile.data.response.ProductInfo
import com.komus.sorage_mobile.data.response.ProductItem

sealed class ProductSearchState {
    object Initial : ProductSearchState()
    object Loading : ProductSearchState()
    data class Success(val products: List<ProductInfo>) : ProductSearchState()
    data class Error(val message: String) : ProductSearchState()
} 