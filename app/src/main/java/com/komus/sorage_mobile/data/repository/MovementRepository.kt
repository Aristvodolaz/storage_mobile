package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.util.ProductMovementHelper
import com.komus.sorage_mobile.util.SPHelper
import timber.log.Timber
import javax.inject.Inject

class MovementRepository @Inject constructor(
    private val api: StorageApi,
    private val spHelper: SPHelper
) {
    suspend fun getLocationItems(locationId: String): LocationItemsResponse {
        val skladId = spHelper.getSkladId()
        return api.getLocationItems(
            locationId = locationId,
            id_sklad = skladId
        )
    }
    
    suspend fun moveProduct(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        quantity: Int,
        conditionState: String,
        expirationDate: String,
        executor: String,
        prunitId: String
    ): BaseResponse {
        val skladId = spHelper.getSkladId()
        
        // Обрабатываем дату через ProductMovementHelper для обеспечения ISO формата
        val isoExpirationDate = ProductMovementHelper.processExpirationDate(expirationDate)
        Timber.d("Перемещение товара с датой в ISO формате: $isoExpirationDate")
        
        val request = MoveProductRequest(
            sourceLocationId = sourceLocationId,
            targetLocationId = targetLocationId,
            prunitId = prunitId,
            quantity = quantity,
            conditionState = conditionState,
            expirationDate = isoExpirationDate,
            executor = executor,
            skladId = skladId,
            productId = productId,
            targetShk = targetLocationId
        )
        
        return api.moveProduct(productId, request)
    }
} 