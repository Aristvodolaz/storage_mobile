package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.response.BaseResponse
import javax.inject.Inject

class PickFromLocationBySkladIdUseCase @Inject constructor(
    private val pickRepository: PickRepository
) {
    suspend operator fun invoke(
        productId: String,
        wrShk: String,
        prunitId: String,
        quantity: Int,
        executor: String,
        skladId: String
    ): BaseResponse {
        return pickRepository.pickFromLocationBySkladId(
            productId = productId,
            wrShk = wrShk,
            prunitId = prunitId,
            quantity = quantity,
            executor = executor,
            skladId = skladId
        )
    }
} 