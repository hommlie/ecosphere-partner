package com.ecosphere.partner.feature.tracking.di

import com.ecosphere.partner.feature.tracking.repository.FirebaseTrackingRepository
import com.ecosphere.partner.feature.tracking.repository.TrackingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(
        impl: FirebaseTrackingRepository
    ): TrackingRepository
}