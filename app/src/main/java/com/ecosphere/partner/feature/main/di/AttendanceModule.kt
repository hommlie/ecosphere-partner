package com.ecosphere.partner.feature.main.di

import com.ecosphere.partner.feature.main.repository.AttendanceRepository
import com.ecosphere.partner.feature.main.repository.FakeAttendanceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AttendanceModule {

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        repository: FakeAttendanceRepository
    ): AttendanceRepository

}