package com.komus.sorage_mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class InventorySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: InventoryRepository, // Внедрение репозитория
    private val networkUtils: NetworkUtils // Внедрение утилиты для проверки сети
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "inventory_sync_worker"
        private const val TAG = "InventorySyncWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting inventory sync")
            if (networkUtils.isNetworkAvailable()) {
                repository.syncInventory()
                Log.d(TAG, "Inventory sync completed successfully")
                return@withContext Result.success()
            } else {
                Log.d(TAG, "No network, retrying sync")
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during inventory sync", e)
            return@withContext Result.retry() // Retry on error
        }
    }
}
