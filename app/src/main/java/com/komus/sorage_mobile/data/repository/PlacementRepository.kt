package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.PlacementDao
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.data.response.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementRepository @Inject constructor(
    private val api: StorageApi,
    private val placementDao: PlacementDao
) {
    suspend fun placeProductToBuffer(
        productId: String,
        prunitId: String,
        quantity: Int,
        conditionState: String,
        expirationDate: String,
        executor: String,
        wrShk: String? = "",
        name: String? = "",
        shk: String? = "",
        article: String? = "",
        skladId: String? = "",
        reason: String? = "",
        productQnt: Int
    ): Result<BaseResponse> {
        return try {
            Log.d(TAG, "Отправка запроса на размещение в буфер: productId=$productId, wrShk=$wrShk, skladId=$skladId, reason=$reason")

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
                Log.d(TAG, "Товар успешно размещен в буфер: ${response.message}")
                Result.success(response)
            } else {
                Log.e(TAG, "Ошибка размещения товара: ${response.message}")
                Result.failure(Exception(response.message ?: "Ошибка размещения товара"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при размещении товара: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun syncPlacements() {
        try {
            val unsyncedPlacements = placementDao.getUnsyncedPlacements()
            
            if (unsyncedPlacements.isNotEmpty()) {
                Log.d(TAG, "Найдено ${unsyncedPlacements.size} несинхронизированных размещений")
                
                for (placement in unsyncedPlacements) {
                    try {
                        val result = placeProductToBuffer(
                            productId = placement.productId,
                            prunitId = placement.prunitId,
                            quantity = placement.quantity,
                            conditionState = placement.conditionState,
                            expirationDate = placement.expirationDate,
                            executor = placement.executor,
                            wrShk = placement.wrShk,
                            name = placement.name,
                            shk = placement.shk,
                            article = placement.article,
                            skladId = placement.skladId,
                            reason = placement.reason,
                            productQnt = placement.productQnt
                        )
                        
                        if (result.isSuccess) {
                            placementDao.markAsSynced(placement.id)
                            Log.d(TAG, "Успешно синхронизировано размещение ${placement.id}")
                        } else {
                            Log.e(TAG, "Ошибка синхронизации размещения ${placement.id}: ${result.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при синхронизации размещения ${placement.id}", e)
                    }
                }
            } else {
                Log.d(TAG, "Несинхронизированных размещений не найдено")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка во время синхронизации размещений", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "PlacementRepository"
    }
} 