package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.api.AuthApi
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.dao.PlacementDao
import com.komus.sorage_mobile.data.offline.OfflineQueue
import com.komus.sorage_mobile.data.offline.ResponseCache
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.MovementRepository
import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.domain.repository.AuthRepository
import com.komus.sorage_mobile.domain.repository.SearchRepository
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.data.db.dao.StorageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: AuthApi, cache: ResponseCache): AuthRepository {
        return AuthRepository(apiService, cache)
    }

    @Provides
    @Singleton
    fun provideMovementRepository(
        apiService: StorageApi,
        spHelper: SPHelper,
        offlineQueue: OfflineQueue,
        cache: ResponseCache
    ): MovementRepository {
        return MovementRepository(apiService, spHelper, offlineQueue, cache)
    }

    @Provides
    @Singleton
    fun providePickRepository(
        apiService: StorageApi,
        offlineQueue: OfflineQueue,
        cache: ResponseCache
    ): PickRepository {
        return PickRepository(apiService, offlineQueue, cache)
    }

    @Provides
    @Singleton
    fun provideSearchRepository(apiService: StorageApi, cache: ResponseCache): SearchRepository {
        return SearchRepository(apiService, cache)
    }

    @Provides
    @Singleton
    fun providePlacementRepository(
        api: StorageApi,
        placementDao: PlacementDao,
        offlineQueue: OfflineQueue
    ): PlacementRepository {
        return PlacementRepository(api, placementDao, offlineQueue)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        api: StorageApi,
        dao: InventoryDao,
        spHelper: SPHelper
    ): InventoryRepository {
        return InventoryRepository(api, dao, spHelper)
    }

    @Provides
    @Singleton
    fun provideProductSearchRepository(
        api: StorageApi,
        dao: StorageDao,
        spHelper: SPHelper,
        networkUtils: NetworkUtils,
        cache: ResponseCache
    ): ProductSearchRepository {
        return ProductSearchRepository(api, dao, spHelper, networkUtils, cache)
    }

}
