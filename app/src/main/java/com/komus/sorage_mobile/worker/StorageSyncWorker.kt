package com.komus.sorage_mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.repository.StorageRepository
import com.komus.sorage_mobile.data.repository.OfflinePlacementRepository
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class StorageSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val storageRepository: StorageRepository,
    private val offlinePlacementRepository: OfflinePlacementRepository,
    private val inventoryRepository: InventoryRepository,
    private val networkUtils: NetworkUtils
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "storage_sync_worker"
        private const val TAG = "StorageSyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val isNetworkAvailable = networkUtils.isNetworkAvailable()

            if (isNetworkAvailable) {
                // Синхронизация всех репозиториев
                syncStorageItems()
                syncOfflinePlacements()
                syncInventory()

                return@withContext Result.success()
            } else {
                Log.d(TAG, "Нет подключения к интернету, синхронизация отложена")
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при выполнении синхронизации", e)
            return@withContext Result.failure()
        }
    }

    private suspend fun syncStorageItems() {
        try {
            Log.d(TAG, "Начало синхронизации storage items")
            storageRepository.syncStorageItems()
            Log.d(TAG, "Синхронизация storage items завершена успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при синхронизации storage items", e)
        }
    }

    private suspend fun syncOfflinePlacements() {
        try {
            Log.d(TAG, "Начало синхронизации размещений")
            offlinePlacementRepository.syncPendingPlacements()
            Log.d(TAG, "Синхронизация размещений завершена успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при синхронизации размещений", e)
        }
    }

    private suspend fun syncInventory() {
        try {
            Log.d(TAG, "Начало синхронизации инвентаризации")
            inventoryRepository.syncPendingInventory()
            Log.d(TAG, "Синхронизация инвентаризации завершена успешно")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при синхронизации инвентаризации", e)
        }
    }
}
