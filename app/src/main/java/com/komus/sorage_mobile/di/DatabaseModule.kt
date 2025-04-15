package com.komus.sorage_mobile.di

import android.content.Context
import androidx.room.Room
import com.komus.sorage_mobile.data.db.AppDatabase
import com.komus.sorage_mobile.data.db.dao.InventoryDao
import com.komus.sorage_mobile.data.db.dao.PlacementDao
import com.komus.sorage_mobile.data.db.dao.OfflinePlacementDao
import com.komus.sorage_mobile.data.db.dao.StorageDao
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_1_2
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_1_3
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_1_4
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_1_5
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_2_3
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_3_4
import com.komus.sorage_mobile.data.db.migrations.MIGRATION_4_5
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "storage_database"
        )
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_1_3,
            MIGRATION_1_4,
            MIGRATION_1_5
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(database: AppDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    @Singleton
    fun providePlacementDao(database: AppDatabase): PlacementDao {
        return database.placementDao()
    }

    @Provides
    @Singleton
    fun provideStorageDao(database: AppDatabase): StorageDao {
        return database.storageDao()
    }

    @Provides
    @Singleton
    fun provideOfflinePlacementDao(database: AppDatabase): OfflinePlacementDao {
        return database.offlinePlacementDao()
    }

}

