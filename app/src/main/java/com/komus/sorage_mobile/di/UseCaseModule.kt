package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.repository.PlacementRepository
import com.komus.sorage_mobile.domain.repository.AuthRepository
import com.komus.sorage_mobile.domain.usecase.AuthenticateUseCase
import com.komus.sorage_mobile.domain.usecase.PlaceProductToBufferUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideAuthenticateUseCase(authRepository: AuthRepository): AuthenticateUseCase {
        return AuthenticateUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun providePlaceProductToBufferUseCase(placementRepository: PlacementRepository): PlaceProductToBufferUseCase {
        return PlaceProductToBufferUseCase(placementRepository)
    }

}
