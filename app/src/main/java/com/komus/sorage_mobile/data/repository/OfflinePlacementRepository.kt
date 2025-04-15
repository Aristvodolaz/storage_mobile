package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.db.dao.OfflinePlacementDao
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflinePlacementRepository @Inject constructor(
    private val offlinePlacementDao: OfflinePlacementDao,
    private val storageApi: StorageApi,
    private val networkUtils: NetworkUtils,
    private val spHelper: SPHelper
) {
    fun getUnsyncedPlacements(): Flow<List<OfflinePlacementEntity>> {
        return offlinePlacementDao.getUnsyncedPlacements()
    }

    fun getUnsyncedPlacementsCount(): Flow<Int> {
        return offlinePlacementDao.getUnsyncedPlacementsCount()
    }

    suspend fun savePlacement(placement: OfflinePlacementEntity) {
        try {
            // Проверка валидности срока годности
            val isExpired = isExpirationDateExpired(placement.endDate)
            if (isExpired && placement.condition == "Кондиция") {
                throw IllegalArgumentException("Нельзя выбрать состояние 'Кондиция' для товара с истекшим сроком годности")
            }
            
            offlinePlacementDao.insertPlacement(placement)
            Log.d("OfflinePlacementRepo", "Placement saved successfully: ${placement.id}")
            
            // Попытка синхронизации сразу после сохранения, если есть интернет
            if (networkUtils.isNetworkAvailable()) {
                syncPlacement(placement)
            }
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error saving placement", e)
            throw e
        }
    }

    // Метод для проверки, истек ли срок годности
    private fun isExpirationDateExpired(expirationDate: String): Boolean {
        try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val expDate = formatter.parse(expirationDate)
            val currentDate = java.util.Date()
            return expDate?.before(currentDate) ?: false
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error parsing expiration date", e)
            return false
        }
    }

    private suspend fun syncPlacement(placement: OfflinePlacementEntity) {
        val maxRetries = 3
        var retryCount = 0
        var lastException: Exception? = null
        
        while (retryCount < maxRetries) {
            try {
                Log.d("OfflinePlacementRepo", "Attempt ${retryCount + 1} to sync placement ${placement.id}")
                
                // Попытка отправить размещение на сервер
                val response = storageApi.placeProductToBuffer(
                    placement.article,
                    PlaceProductRequest(
                        wrShk = placement.cellBarcode,
                        article = placement.article,
                        productQnt = placement.productQnt,
                        quantity = placement.quantity,
                        shk = placement.barcode,
                        reason = placement.reason,
                        conditionState = placement.condition,
                        prunitId = placement.prunitTypeId.toString(),
                        expirationDate = placement.endDate,
                        executor = spHelper.getUserName(),
                        skladId = spHelper.getSkladId()
                    )
                )

                val isSuccess = response.success
                
                if (isSuccess) {
                    offlinePlacementDao.markAsSynced(placement.id)
                    Log.d("OfflinePlacementRepo", "Placement synced successfully: ${placement.id}")
                    return
                } else {
                    val errorMessage = response.message ?: "Неизвестная ошибка"
                    Log.e("OfflinePlacementRepo", "Sync failed: $errorMessage")
                    lastException = Exception(errorMessage)
                    retryCount++
                    kotlinx.coroutines.delay(1000L * retryCount) // Увеличивающаяся задержка между попытками
                }
            } catch (e: Exception) {
                Log.e("OfflinePlacementRepo", "Error syncing placement", e)
                lastException = e
                retryCount++
                kotlinx.coroutines.delay(1000L * retryCount) // Увеличивающаяся задержка между попытками
            }
        }
        
        // После всех попыток сохраняем информацию об ошибке
        offlinePlacementDao.updateSyncAttempt(
            placementId = placement.id,
            timestamp = System.currentTimeMillis(),
            error = lastException?.message ?: "Неизвестная ошибка после $maxRetries попыток"
        )
    }

    suspend fun syncPendingPlacements() {
        if (!networkUtils.isNetworkAvailable()) {
            Log.d("OfflinePlacementRepo", "No network connection available for sync")
            return
        }

        val placementsToSync = offlinePlacementDao.getPlacementsForSync(System.currentTimeMillis())
        for (placement in placementsToSync) {
            syncPlacement(placement)
        }
    }

    suspend fun markAsSynced(placementId: String) {
        try {
            offlinePlacementDao.markAsSynced(placementId)
            Log.d("OfflinePlacementRepo", "Placement marked as synced: $placementId")
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error marking placement as synced", e)
            throw e
        }
    }

    suspend fun cleanupSyncedPlacements() {
        try {
            offlinePlacementDao.deleteSyncedPlacements()
            Log.d("OfflinePlacementRepo", "Synced placements cleaned up successfully")
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error cleaning up synced placements", e)
            throw e
        }
    }

    suspend fun getPlacementById(id: String): OfflinePlacementEntity? {
        return offlinePlacementDao.getPlacementById(id)
    }

    // Метод для перемещения товара
    suspend fun movePlacement(fromCell: String, toCell: String, placement: OfflinePlacementEntity): Result<Boolean> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                Log.d("OfflinePlacementRepo", "No network connection available for move operation")
                // Сохраняем перемещение локально
                val moveOperation = placement.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    cellBarcode = toCell
                )
                offlinePlacementDao.insertPlacement(moveOperation)
                Result.success(true)
            } else {
                try {
                    // Попытка выполнить перемещение онлайн
                    val response = storageApi.moveProductToBuffer(
                        placement.article,
                        PlaceProductRequest(
                            wrShk = toCell,
                            article = placement.article,
                            productQnt = placement.productQnt,
                            quantity = placement.quantity,
                            shk = placement.barcode,
                            reason = placement.reason,
                            conditionState = placement.condition,
                            prunitId = placement.prunitTypeId.toString(),
                            expirationDate = placement.endDate,
                            executor = spHelper.getUserName(),
                            skladId = spHelper.getSkladId()
                        )
                    )
                    
                    if (response.success) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(response.message ?: "Ошибка при перемещении товара"))
                    }
                } catch (e: Exception) {
                    // При ошибке сохраняем локально
                    val moveOperation = placement.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        cellBarcode = toCell
                    )
                    offlinePlacementDao.insertPlacement(moveOperation)
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error during move operation", e)
            Result.failure(e)
        }
    }

    // Метод для снятия товара
    suspend fun removePlacement(placement: OfflinePlacementEntity): Result<Boolean> {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                Log.d("OfflinePlacementRepo", "No network connection available for remove operation")
                // Сохраняем операцию снятия локально
                val removeOperation = placement.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    reason = "Снятие товара"
                )
                offlinePlacementDao.insertPlacement(removeOperation)
                Result.success(true)
            } else {
                try {
                    // Попытка выполнить снятие онлайн
                    val response = storageApi.removeProductFromBuffer(
                        placement.article,
                        PlaceProductRequest(
                            wrShk = placement.cellBarcode,
                            article = placement.article,
                            productQnt = placement.productQnt,
                            quantity = placement.quantity,
                            shk = placement.barcode,
                            reason = "Снятие товара",
                            conditionState = placement.condition,
                            prunitId = placement.prunitTypeId.toString(),
                            expirationDate = placement.endDate,
                            executor = spHelper.getUserName(),
                            skladId = spHelper.getSkladId()
                        )
                    )
                    
                    if (response.success) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(response.message ?: "Ошибка при снятии товара"))
                    }
                } catch (e: Exception) {
                    // При ошибке сохраняем локально
                    val removeOperation = placement.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        reason = "Снятие товара"
                    )
                    offlinePlacementDao.insertPlacement(removeOperation)
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            Log.e("OfflinePlacementRepo", "Error during remove operation", e)
            Result.failure(e)
        }
    }
} 