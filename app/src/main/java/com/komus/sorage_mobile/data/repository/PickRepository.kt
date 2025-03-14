package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.LocationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PickRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun getLocationItems(locationId: String): Flow<Result<List<LocationItem>>> = flow {
        try {
            Log.d("PickRepository", "Запрос товаров в ячейке: $locationId")
            val response = withContext(Dispatchers.IO) {
                api.getLocationItems(locationId)
            }
            
            if (response.success) {
                Log.d("PickRepository", "Получено ${response.data.size} товаров в ячейке")
                emit(Result.success(response.data))
            } else {
                Log.e("PickRepository", "Ошибка получения товаров: ${response.message}")
                emit(Result.failure(Exception(response.message ?: "Ошибка получения товаров")))
            }
        } catch (e: Exception) {
            Log.e("PickRepository", "Исключение при получении товаров: ${e.message}")
            emit(Result.failure(e))
        }
    }
    
    suspend fun pickProduct(
        productId: String,
        locationId: String,
        prunitId: String,
        quantity: Int,
        executor: String
    ): BaseResponse {
        Log.d("PickRepository", "Снятие товара: productId=$productId, locationId=$locationId, quantity=$quantity")
        
        val request = PickRequest(
            locationId = locationId,
            prunitId = prunitId,
            quantity = quantity,
            executor = executor
        )
        
        return try {
            withContext(Dispatchers.IO) {
                api.pickProduct(productId, request)
            }
        } catch (e: Exception) {
            Log.e("PickRepository", "Ошибка при снятии товара: ${e.message}")
            BaseResponse(success = false, message = e.message)
        }
    }
} 