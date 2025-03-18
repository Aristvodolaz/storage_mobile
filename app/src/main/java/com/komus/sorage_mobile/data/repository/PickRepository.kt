package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.MoveProductRequest
import com.komus.sorage_mobile.data.request.PickFromLocationRequest
import com.komus.sorage_mobile.data.request.PickRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import com.komus.sorage_mobile.data.response.LocationItem
import com.komus.sorage_mobile.data.response.LocationItemsResponse
import com.komus.sorage_mobile.util.ProductMovementHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PickRepository @Inject constructor(
    private val api: StorageApi
) {
    suspend fun getLocationItems(locationId: String, sklad:String): Flow<Result<List<LocationItem>>> = flow {
        try {
            Log.d("PickRepository", "Запрос товаров в ячейке: $locationId")
            val response = withContext(Dispatchers.IO) {
                api.getLocationItems(locationId, sklad)
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
    
    suspend fun pickFromLocation(
        productId: String,
        wrShk: String,
        prunitId: String,
        quantity: Int,
        executor: String,
        skladId: String
    ): BaseResponse {
        Log.d("PickRepository", "Снятие товара из ячейки: productId=$productId, wrShk=$wrShk, quantity=$quantity")
        
        val request = PickFromLocationRequest(
            productId = productId,
            WR_SHK = wrShk,
            prunitId = prunitId,
            quantity = quantity,
            executor = executor,
            sklad_id = skladId
        )
        
        return try {
            withContext(Dispatchers.IO) {
                api.pickFromLocation(request)
            }
        } catch (e: Exception) {
            Log.e("PickRepository", "Ошибка при снятии товара из ячейки: ${e.message}")
            BaseResponse(success = false, message = e.message)
        }
    }
    
    suspend fun pickFromLocationBySkladId(
        productId: String,
        wrShk: String,
        prunitId: String,
        quantity: Int,
        executor: String,
        skladId: String
    ): BaseResponse {
        Log.d("PickRepository", "Снятие товара из ячейки с учетом склада: productId=$productId, wrShk=$wrShk, skladId=$skladId, quantity=$quantity")
        
        val request = PickFromLocationRequest(
            productId = productId,
            WR_SHK = wrShk,
            prunitId = prunitId,
            quantity = quantity,
            executor = executor,
            sklad_id = skladId
        )
        
        return try {
            withContext(Dispatchers.IO) {
                api.pickFromLocationBySkladId(request)
            }
        } catch (e: Exception) {
            Log.e("PickRepository", "Ошибка при снятии товара из ячейки с учетом склада: ${e.message}")
            BaseResponse(success = false, message = e.message)
        }
    }
    
    suspend fun moveProduct(
        productId: String,
        sourceLocationId: String,
        targetLocationId: String,
        prunitId: String,
        quantity: Int,
        conditionState: String,
        executor: String,
        skladId: String,
        expirationDate: String
    ): BaseResponse {
        // Обрабатываем дату через ProductMovementHelper для обеспечения ISO формата
        val isoExpirationDate = ProductMovementHelper.processExpirationDate(expirationDate)
        Timber.d("Перемещение товара с датой в ISO формате: $isoExpirationDate")
        
        val request = MoveProductRequest(
            sourceLocationId = sourceLocationId,
            targetLocationId = targetLocationId,
            prunitId = prunitId,
            quantity = quantity,
            conditionState = conditionState,
            executor = executor,
            skladId = skladId,
            expirationDate = isoExpirationDate,
            productId = productId,
            targetShk = targetLocationId
        )
        
        return try {
            val response = api.moveProduct(productId, request)
            Timber.d("Результат перемещения товара: success=${response.success}, message=${response.message}")
            response
        } catch (e: Exception) {
            Timber.e(e, "Ошибка при перемещении товара")
            BaseResponse(success = false, message = e.message)
        }
    }
} 