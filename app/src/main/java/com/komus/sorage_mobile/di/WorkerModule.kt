package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.StorageRepository
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.worker.StorageWorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkerFactory(
        storageRepository: StorageRepository,
        inventoryRepository: InventoryRepository,
        networkUtils: NetworkUtils,
        offlineQueue: OfflineQueue
    ): StorageWorkerFactory {
        return StorageWorkerFactory(storageRepository, inventoryRepository, networkUtils, offlineQueue)
    }
}
