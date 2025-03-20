package com.komus.sorage_mobile.util

import android.content.Context
import androidx.work.*
import com.komus.sorage_mobile.worker.InventorySyncWorker
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val networkUtils: NetworkUtils
) {
    fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<InventorySyncWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            InventorySyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    fun requestImmediateSync() {
        if (!networkUtils.isNetworkAvailable()) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<InventorySyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${InventorySyncWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun getWorkInfo() = WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(InventorySyncWorker.WORK_NAME)
}
