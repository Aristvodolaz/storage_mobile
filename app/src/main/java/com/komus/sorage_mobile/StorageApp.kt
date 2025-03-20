package com.komus.sorage_mobile

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.komus.sorage_mobile.worker.StorageSyncWorker
import com.komus.sorage_mobile.worker.StorageWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class StorageApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: StorageWorkerFactory


    override fun onCreate() {
        super.onCreate()
        setupStorageSync()
    }

    // Use the workManagerConfiguration property instead of getWorkManagerConfiguration method
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun setupStorageSync() {
        // Create a one-time sync request when the app is launched
        val syncRequest = OneTimeWorkRequestBuilder<StorageSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // Create a periodic sync request every 6 hours
        val periodicSyncRequest = PeriodicWorkRequestBuilder<StorageSyncWorker>(
            6, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flexible execution interval
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(this).apply {
            // Start one-time sync when the app launches
            enqueueUniqueWork(
                "${StorageSyncWorker.WORK_NAME}_onetime",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

            // Start periodic sync
            enqueueUniquePeriodicWork(
                StorageSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // Keep the existing task
                periodicSyncRequest
            )
        }
    }
}
