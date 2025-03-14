package com.komus.sorage_mobile.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Здесь вызываем API и синхронизируем данные
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
