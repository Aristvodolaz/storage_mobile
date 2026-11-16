package com.komus.sorage_mobile.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.StorageRepository
import com.komus.sorage_mobile.util.NetworkUtils
import javax.inject.Inject

class StorageWorkerFactory @Inject constructor(
    private val storageRepository: StorageRepository,
    private val inventoryRepository: InventoryRepository,
    private val networkUtils: NetworkUtils,
    private val offlineQueue: OfflineQueue
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            StorageSyncWorker::class.java.name ->
                StorageSyncWorker(appContext, workerParameters, storageRepository, inventoryRepository, networkUtils)
            InventorySyncWorker::class.java.name ->
                InventorySyncWorker(appContext, workerParameters, inventoryRepository, networkUtils)
            OfflineQueueWorker::class.java.name ->
                OfflineQueueWorker(appContext, workerParameters, offlineQueue)
            else -> null
        }
    }
}
