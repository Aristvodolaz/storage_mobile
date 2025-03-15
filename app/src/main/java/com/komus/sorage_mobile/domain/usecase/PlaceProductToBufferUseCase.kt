package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.data.response.BaseResponse
import javax.inject.Inject

class PlaceProductToBufferUseCase @Inject constructor(
    private val repository: PlacementRepository
) {
    suspend fun execute(
        productId: String,
        prunitId: String,
        quantity: Int,
        conditionState: String,
        expirationDate: String,
        executor: String,
        wrShk: String? = null,
        name: String? = null,
        shk: String? = null,
        article: String? = null,
        skladId: String? = null
    ): Result<BaseResponse> {
        return repository.placeProductToBuffer(
            productId = productId,
            prunitId = prunitId,
            quantity = quantity,
            conditionState = conditionState,
            expirationDate = expirationDate,
            executor = executor,
            wrShk = wrShk,
            name = name,
            shk = shk,
            article = article,
            skladId = skladId
        )
    }
} 