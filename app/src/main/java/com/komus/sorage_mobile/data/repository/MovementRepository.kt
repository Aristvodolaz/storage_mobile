package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.response.BaseResponse
import javax.inject.Inject

class MovementRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun moveProduct(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        quantity: Int
    ): BaseResponse {
        val requestBody = mapOf(
            "sourceLocationId" to sourceLocationId,
            "targetLocationId" to targetLocationId,
            "quantity" to quantity,
            "conditionState" to "кондиция", // Значение по умолчанию
            "expirationDate" to "2025-01-14", // Значение по умолчанию
            "executor" to "string" // Значение по умолчанию
        )
        
        return api.moveProduct(productId, requestBody)
    }
} 