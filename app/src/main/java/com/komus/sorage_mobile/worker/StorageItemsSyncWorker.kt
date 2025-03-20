package com.komus.sorage_mobile.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.repository.StorageRepository
import com.komus.sorage_mobile.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StorageItemsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val storageRepository: StorageRepository,
    private val networkUtils: NetworkUtils
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "storage_items_sync_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                Log.d(WORK_NAME, "Нет подключения к интернету, синхронизация отложена")
                return Result.retry()
            }

            Log.d(WORK_NAME, "Начало синхронизации storage items")
            storageRepository.syncStorageItems()
            Log.d(WORK_NAME, "Синхронизация storage items завершена успешно")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(WORK_NAME, "Ошибка при синхронизации storage items", e)
            Result.failure()
        }
    }
} 