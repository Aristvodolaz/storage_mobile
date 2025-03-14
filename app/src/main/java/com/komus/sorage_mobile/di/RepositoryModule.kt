package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.data.repository.MovementRepository
import com.komus.sorage_mobile.data.repository.PickRepository
import com.komus.sorage_mobile.data.repository.ProductSearchRepository
import com.komus.sorage_mobile.domain.repository.AuthRepository
import com.komus.sorage_mobile.domain.repository.SearchRepository
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
    fun provideMovementRepository(apiService: StorageApi): MovementRepository {
        return MovementRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun providePickRepository(apiService: StorageApi): PickRepository {
        return PickRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideProductSearchRepository(apiService: StorageApi): ProductSearchRepository {
        return ProductSearchRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideSearchRepository(apiService: StorageApi): SearchRepository {
        return SearchRepository(apiService)
    }
}
