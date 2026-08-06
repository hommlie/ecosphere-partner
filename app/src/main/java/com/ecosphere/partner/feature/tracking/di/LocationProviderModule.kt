package com.ecosphere.partner.feature.tracking.di

import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import com.ecosphere.partner.feature.tracking.location.DefaultLocationProvider
import com.ecosphere.partner.feature.tracking.location.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationProviderModule {

    @Provides
    @Singleton
    fun provideFusedLocationProvider(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {

        return LocationServices.getFusedLocationProviderClient(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationBindingModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(
        impl: DefaultLocationProvider
    ): LocationProvider
}