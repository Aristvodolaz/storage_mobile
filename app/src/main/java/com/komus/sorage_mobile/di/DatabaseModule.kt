package com.komus.sorage_mobile.di

import android.content.Context
import androidx.room.Room
import com.komus.sorage_mobile.data.db.AppDatabase
import com.komus.sorage_mobile.data.db.StorageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
//    @Provides
//    @Singleton
//    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
//        return Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            "storage_database"
//        ).build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideInformationDao(database: AppDatabase): StorageDao {
//        return database.informationDao()
//    }
//
//    @Provides
//    @Singleton
//    fun provideInformationRepository(informationDao: InformationDao): InformationRepository {
//        return InformationRepository(informationDao)
//    }
}

