package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.db.AppDatabase
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.dao.PlacementDao
import com.komus.sorage_mobile.data.repository.InventoryRepository
import com.komus.sorage_mobile.data.repository.MovementRepository
import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.domain.repository.AuthRepository
import com.komus.sorage_mobile.domain.repository.SearchRepository
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.data.repository.OfflinePlacementRepository
import com.komus.sorage_mobile.data.db.dao.OfflinePlacementDao
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
    fun provideAuthRepository(apiService: StorageApi): AuthRepository {
        return AuthRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideMovementRepository(apiService: StorageApi, spHelper: SPHelper): MovementRepository {
        return MovementRepository(apiService, spHelper)
    }
    
    @Provides
    @Singleton
    fun providePickRepository(apiService: StorageApi): PickRepository {
        return PickRepository(apiService)
    }

    
    @Provides
    @Singleton
    fun provideSearchRepository(apiService: StorageApi): SearchRepository {
        return SearchRepository(apiService)
    }

    @Provides
    @Singleton
    fun providePlacementRepository(
        api: StorageApi,
        placementDao: PlacementDao
    ): PlacementRepository {
        return PlacementRepository(api, placementDao)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(
        api: StorageApi,
        dao: InventoryDao,
        spHelper: SPHelper,
        networkUtils: NetworkUtils
    ): InventoryRepository {
        return InventoryRepository(api, dao, spHelper, networkUtils)
    }
    @Provides
    @Singleton
    fun provideProductSearchRepository(
        api: StorageApi,
        dao: StorageDao,
        spHelper: SPHelper,
        networkUtils: NetworkUtils
    ): ProductSearchRepository {
        return ProductSearchRepository(api, dao, spHelper, networkUtils)
    }
    @Provides
    @Singleton
    fun provideOfflinePlacementRepository(
        offlinePlacementDao: OfflinePlacementDao,
        storageApi: StorageApi,
        networkUtils: NetworkUtils,
        spHelper: SPHelper
    ): OfflinePlacementRepository {
        return OfflinePlacementRepository(offlinePlacementDao, storageApi, networkUtils, spHelper)
    }
}
