package com.komus.sorage_mobile.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.komus.sorage_mobile.data.offline.OfflineQueue
import java.util.concurrent.TimeUnit

/** Отправляет накопленные офлайн-операции, когда есть сеть. */
class OfflineQueueWorker(
    context: Context,
    params: WorkerParameters,
    private val offlineQueue: OfflineQueue
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return if (offlineQueue.drain()) Result.success() else Result.retry()
    }

    companion object {
        const val ONE_TIME_WORK_NAME = "offline_queue_drain"
        const val PERIODIC_WORK_NAME = "offline_queue_periodic"

        private val connected = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        /** Ставит разовую отправку в конец текущей цепочки; запускается сразу после появления сети. */
        fun enqueueNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<OfflineQueueWorker>()
                .setConstraints(connected)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }

        /** Периодическая страховка на случай, если разовый запуск не сработал. */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<OfflineQueueWorker>(15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
                .setConstraints(connected)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
