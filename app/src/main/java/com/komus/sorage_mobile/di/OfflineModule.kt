package com.komus.sorage_mobile.di

import com.komus.sorage_mobile.data.offline.DrainScheduler
import com.komus.sorage_mobile.data.offline.WorkManagerDrainScheduler
import com.komus.sorage_mobile.util.SPHelper
import com.komus.sorage_mobile.util.UserSession
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OfflineModule {
    @Binds
    abstract fun bindDrainScheduler(impl: WorkManagerDrainScheduler): DrainScheduler

    companion object {
        @Provides
        fun provideUserSession(spHelper: SPHelper): UserSession = spHelper
    }
}
