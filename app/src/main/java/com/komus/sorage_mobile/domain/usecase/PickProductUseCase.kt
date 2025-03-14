package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.response.BaseResponse
import javax.inject.Inject

class PickProductUseCase @Inject constructor(
    private val pickRepository: PickRepository
) {
    suspend operator fun invoke(
        productId: String,
        locationId: String,
        prunitId: String,
        quantity: Int,
        executor: String
    ): BaseResponse {
        return pickRepository.pickProduct(
            productId = productId,
            locationId = locationId,
            prunitId = prunitId,
            quantity = quantity,
            executor = executor
        )
    }
} 