package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.OfflinePlacementRepository
import com.komus.sorage_mobile.data.repository.PlacementRepository
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
        placementRepository: PlacementRepository,
        offlinePlacementRepository: OfflinePlacementRepository,
        inventoryRepository: InventoryRepository,
        networkUtils: NetworkUtils
    ): StorageWorkerFactory {
        return StorageWorkerFactory(
            storageRepository,
            placementRepository,
            offlinePlacementRepository,
            inventoryRepository,
            networkUtils
        )
    }
}
