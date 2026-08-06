package com.ecosphere.partner.feature.tracking.di

import com.ecosphere.partner.feature.tracking.manager.DefaultTrackingManager
import com.ecosphere.partner.feature.tracking.manager.TrackingManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingManagerModule {

    @Binds
    @Singleton
    abstract fun bindTrackingManager(
        impl: DefaultTrackingManager
    ): TrackingManager
}