package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PlacementRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun placeProductToBuffer(
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
        skladId: String? = null,
        reason: String? = null,
        productQnt: Int
    ): Result<BaseResponse> {
        return try {
            Log.d("PlacementRepository", "Размещение товара $productId в буфер")
            
            val request = PlaceProductRequest(
                prunitId = prunitId,
                quantity = quantity,
                conditionState = conditionState,
                expirationDate = expirationDate,
                executor = executor,
                wrShk = wrShk,
                name = name,
                shk = shk,
                article = article,
                skladId = skladId,
                reason = reason,
                productQnt = productQnt
            )
            
            val response = withContext(Dispatchers.IO) {
                api.placeProductToBuffer(productId, request)
            }
            
            if (response.success) {
                Log.d("PlacementRepository", "Товар успешно размещен в буфер")
                Result.success(response)
            } else {
                Log.e("PlacementRepository", "Ошибка размещения товара: ${response.message}")
                Result.failure(Exception(response.message ?: "Ошибка размещения товара"))
            }
        } catch (e: Exception) {
            Log.e("PlacementRepository", "Исключение при размещении товара: ${e.message}")
            Result.failure(e)
        }
    }
} 