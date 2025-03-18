package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.repository.MovementRepository
import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.domain.repository.AuthRepository
import com.komus.sorage_mobile.domain.repository.SearchRepository
import com.komus.sorage_mobile.util.SPHelper
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
    fun provideProductSearchRepository(apiService: StorageApi, spHelper: SPHelper): ProductSearchRepository {
        return ProductSearchRepository(apiService, spHelper)
    }
    
    @Provides
    @Singleton
    fun provideSearchRepository(apiService: StorageApi): SearchRepository {
        return SearchRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun providePlacementRepository(apiService: StorageApi): PlacementRepository {
        return PlacementRepository(apiService)
    }
}
