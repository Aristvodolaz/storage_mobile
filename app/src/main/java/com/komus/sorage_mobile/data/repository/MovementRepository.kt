package com.komus.sorage_mobile.data.repository

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.offline.OfflineOpType
import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.util.ProductMovementHelper
import com.komus.sorage_mobile.util.SPHelper
import timber.log.Timber
import javax.inject.Inject

class MovementRepository @Inject constructor(
    private val api: StorageApi,
    private val spHelper: SPHelper,
    private val offlineQueue: OfflineQueue,
    private val cache: ResponseCache
) {
    suspend fun getLocationItems(locationId: String): LocationItemsResponse {
        val skladId = spHelper.getSkladId()
        return cache.fetch(
            key = "location-items:$locationId:$skladId",
            clazz = LocationItemsResponse::class.java,
            isCacheable = { it.success }
        ) {
            api.getLocationItems(
                locationId = locationId,
                id_sklad = skladId
            )
        }
    }

    suspend fun moveProduct(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        quantity: Int,
        conditionState: String,
        expirationDate: String,
        executor: String,
        prunitId: String,
        productQnt: String,
        reason: String,
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
            targetShk = targetLocationId,
            productQnt = productQnt,
            reason = reason
        )

        return offlineQueue.sendOrQueue(OfflineOpType.MOVE, productId, request) { key ->
            api.moveProduct(productId, request, key)
        }
    }
}
