package com.komus.sorage_mobile.di

import android.content.Context
import com.komus.sorage_mobile.data.api.AuthApi
import com.komus.sorage_mobile.data.api.StorageApi
import com.komus.sorage_mobile.util.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }


    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

    @Singleton
    @Provides
    @Named("StorageRetrofit")
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("http://31.128.44.48:3006/")
            .baseUrl("http://10.171.12.36:3006/")

            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named("AuthRetrofit")
    fun provideAuthRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("http://31.128.44.48:3005/")
            .baseUrl("http://10.171.12.36:3005/")

            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStorageApi(@Named("StorageRetrofit") retrofit: Retrofit): StorageApi {
        return retrofit.create(StorageApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(@Named("AuthRetrofit") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}

