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
    private suspend fun syncPlacement(placement: OfflinePlacementEntity) {
        try {
            // Попытка отправить размещение на сервер
            // Адаптируйте под реальное API
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

            val isSuccess = response != null

            if (isSuccess) {
                offlinePlacementDao.markAsSynced(placement.id)
                Log.d("OfflinePlacementRepo", "Placement synced successfully: ${placement.id}")
            } else {
                offlinePlacementDao.updateSyncAttempt(
                    placementId = placement.id,
                    timestamp = System.currentTimeMillis(),
                    error = "Ошибка синхронизации"
                )
                Log.e("OfflinePlacementRepo", "Sync failed")
            }
        } catch (e: Exception) {
            offlinePlacementDao.updateSyncAttempt(
                placementId = placement.id,
                timestamp = System.currentTimeMillis(),
                error = e.message
            )
            Log.e("OfflinePlacementRepo", "Error syncing placement", e)
        }
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
} 