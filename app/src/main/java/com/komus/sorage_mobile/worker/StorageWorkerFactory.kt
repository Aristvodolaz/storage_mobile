package com.komus.sorage_mobile.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.OfflinePlacementRepository
import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.data.repository.StorageRepository
import com.komus.sorage_mobile.util.NetworkUtils
import javax.inject.Inject

class StorageWorkerFactory @Inject constructor(
    private val storageRepository: StorageRepository,
    private val placementRepository: PlacementRepository,
    private val offlinePlacementRepository: OfflinePlacementRepository,
    private val inventoryRepository: InventoryRepository,
    private val networkUtils: NetworkUtils
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            StorageSyncWorker::class.java.name ->
                StorageSyncWorker(appContext, workerParameters, storageRepository, offlinePlacementRepository, inventoryRepository, networkUtils)
            InventorySyncWorker::class.java.name ->
                InventorySyncWorker(appContext, workerParameters, inventoryRepository, networkUtils)
            PlacementSyncWorker::class.java.name ->
                PlacementSyncWorker(appContext, workerParameters, offlinePlacementRepository, networkUtils)
            else -> null
        }
    }
}
