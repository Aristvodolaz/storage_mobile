package com.komus.sorage_mobile.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.repository.OfflinePlacementRepository
import com.komus.sorage_mobile.util.NetworkUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PlacementSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlinePlacementRepository: OfflinePlacementRepository,
    private val networkUtils: NetworkUtils
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "placement_sync_worker"
    }

    override suspend fun doWork(): Result {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                return Result.retry()
            }

            offlinePlacementRepository.syncPendingPlacements()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
