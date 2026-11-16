package com.komus.sorage_mobile.data.offline

import android.content.Context
import com.komus.sorage_mobile.worker.OfflineQueueWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Просит фоновую отправку очереди, когда появится сеть. */
interface DrainScheduler {
    fun requestDrain()
}

@Singleton
class WorkManagerDrainScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : DrainScheduler {
    override fun requestDrain() {
        OfflineQueueWorker.enqueueNow(context)
    }
}
