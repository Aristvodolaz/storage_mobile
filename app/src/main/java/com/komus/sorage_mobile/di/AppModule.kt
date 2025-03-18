package com.komus.sorage_mobile.di

import android.content.Context
import android.content.SharedPreferences
import com.komus.sorage_mobile.util.NetworkUtils
import com.komus.sorage_mobile.util.SPHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("storage_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSPHelper(sharedPreferences: SharedPreferences): SPHelper {
        return SPHelper(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }


} 