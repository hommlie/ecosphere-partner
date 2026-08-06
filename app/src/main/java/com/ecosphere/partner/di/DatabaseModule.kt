package com.ecosphere.partner.di

import android.content.Context
import androidx.room.Room
import com.ecosphere.partner.data.local.db.AppDatabase
import com.ecosphere.partner.feature.tracking.offline.dao.QueuedTrackingPointDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "physio_partner.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        return Room.databaseBuilder(

            context,

            AppDatabase::class.java,

            DATABASE_NAME

        ).build()

    }

    @Provides
    fun provideQueuedTrackingDao(
        database: AppDatabase
    ): QueuedTrackingPointDao {

        return database.queuedTrackingPointDao()

    }

}