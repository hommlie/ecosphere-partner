package com.ecosphere.partner.feature.tracking.recovery

import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingExceptionHandler @Inject constructor(

    private val crashRecoveryManager: CrashRecoveryManager

) {

    fun create() = CoroutineExceptionHandler { _, _ ->

        // Reserved for future logging (Crashlytics/Sentry)

    }

}