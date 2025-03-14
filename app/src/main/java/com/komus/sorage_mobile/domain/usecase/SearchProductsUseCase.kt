package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.data.response.ProductItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val productSearchRepository: ProductSearchRepository
) {
    suspend operator fun invoke(query: String): Flow<Result<List<ProductItem>>> {
        return productSearchRepository.searchProducts(query)
    }
} 