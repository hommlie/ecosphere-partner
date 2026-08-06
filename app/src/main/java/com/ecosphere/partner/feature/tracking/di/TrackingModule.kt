package com.ecosphere.partner.feature.tracking.di

import com.ecosphere.partner.feature.tracking.utils.DefaultDistanceCalculator
import com.ecosphere.partner.feature.tracking.utils.DistanceCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackingModule {

    @Binds
    @Singleton
    abstract fun bindDistanceCalculator(
        impl: DefaultDistanceCalculator
    ): DistanceCalculator
}