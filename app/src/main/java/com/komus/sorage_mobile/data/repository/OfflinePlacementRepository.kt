package com.komus.sorage_mobile.data.repository

import android.util.Log
import com.komus.sorage_mobile.data.db.dao.OfflinePlacementDao
import com.komus.sorage_mobile.data.db.entity.OfflinePlacementEntity
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.request.PlaceProductRequest
import com.komus.sorage_mobile.domain.util.ExpirationDateValidator
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflinePlacementRepository @Inject constructor(
    private val offlinePlacementDao: OfflinePlacementDao,
    private val storageApi: StorageApi,
    private val networkUtils: NetworkUtils,
    private val spHelper: SPHelper
) {
    private val _bufferItems = MutableStateFlow<List<BufferItem>>(emptyList())
    val bufferItems: StateFlow<List<BufferItem>> = _bufferItems.asStateFlow()

    fun getUnsyncedPlacements(): Flow<List<OfflinePlacementEntity>> {
        return offlinePlacementDao.getUnsyncedPlacements()
    }

    fun getUnsyncedPlacementsCount(): Flow<Int> {
        return offlinePlacementDao.getUnsyncedPlacementsCount()
    }

    suspend fun savePlacement(placement: OfflinePlacementEntity) {
        try {
            // Проверка валидности срока годности для указанного состояния
            if (!ExpirationDateValidator.isValidForCondition(placement.endDate, placement.condition)) {
                throw IllegalArgumentException("Невозможно разместить товар с истекшим сроком годности в состоянии 'Кондиция'")
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

    suspend fun moveProductToBuffer(
        productId: Long,
        prunitId: Int,
        quantity: Int,
        conditionState: String,
        expirationDate: String,
        executor: String,
        wrShk: String,
        name: String,
        shk: String,
        article: String,
        skladId: Int,
        reason: String,
        productQnt: Int
    ): Result<String> {
        return try {
            val newItem = BufferItem(
                id = productId,
                name = name,
                article = article,
                shk = shk,
                quantity = quantity,
                prunitId = prunitId,
                wrShk = wrShk,
                idScklad = skladId,
                conditionState = conditionState,
                expirationDate = expirationDate,
                executor = executor,
                reason = reason,
                productQnt = productQnt
            )
            
            val currentItems = _bufferItems.value.toMutableList()
            currentItems.add(newItem)
            _bufferItems.value = currentItems
            
            Result.success("Товар успешно добавлен в буфер")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeProductFromBuffer(productId: Long): Result<String> {
        return try {
            val currentItems = _bufferItems.value.toMutableList()
            val removed = currentItems.removeAll { it.id == productId }
            
            if (removed) {
                _bufferItems.value = currentItems
                Result.success("Товар успешно удален из буфера")
            } else {
                Result.failure(Exception("Товар не найден в буфере"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearBuffer() {
        _bufferItems.value = emptyList()
    }

    fun getBufferItemCount(): Int {
        return _bufferItems.value.size
    }

    fun hasItemInBuffer(productId: Long): Boolean {
        return _bufferItems.value.any { it.id == productId }
    }
}

data class BufferItem(
    val id: Long,
    val name: String,
    val article: String,
    val shk: String,
    val quantity: Int,
    val prunitId: Int,
    val wrShk: String,
    val idScklad: Int,
    val conditionState: String,
    val expirationDate: String,
    val executor: String,
    val reason: String,
    val productQnt: Int
) 