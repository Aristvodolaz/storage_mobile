package com.komus.sorage_mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import com.komus.sorage_mobile.util.SPHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StorageApp: Application() {
    @Inject
    lateinit var spHelper: SPHelper

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
}