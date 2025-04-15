package com.komus.sorage_mobile.domain.usecase

import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.util.ProductMovementHelper
import timber.log.Timber
import javax.inject.Inject

class MoveProductUseCase @Inject constructor(
    private val pickRepository: PickRepository
) {
    suspend operator fun invoke(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        prunitId: String,
        quantity: Int,
        conditionState: String,
        executor: String,
        skladId: String,
        expirationDate: String,
        productQnt: String,
        reason: String
    ): BaseResponse {
        // Обрабатываем дату через ProductMovementHelper для обеспечения ISO формата
        val isoExpirationDate = ProductMovementHelper.processExpirationDate(expirationDate)
        Timber.d("Перемещение товара с датой: $isoExpirationDate")
        
        return pickRepository.moveProduct(
            productId = productId,
            sourceLocationId = sourceLocationId,
            targetLocationId = targetLocationId,
            prunitId = prunitId,
            quantity = quantity,
            conditionState = conditionState,
            executor = executor,
            skladId = skladId,
            expirationDate = isoExpirationDate,
            productQnt = productQnt,
            reason = reason
        )
    }
} 