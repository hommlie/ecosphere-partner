package com.ecosphere.partner.feature.tracking.recovery

import android.content.Context
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.scheduler.TrackingScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashRecoveryManager @Inject constructor(

    @ApplicationContext
    private val context: Context,

    private val sessionRestoreManager: SessionRestoreManager,

    private val scheduler: TrackingScheduler,
    private val permissionManager: PermissionManager

) {

    suspend fun recoverAfterCrash() {

        if (!permissionManager.hasLocationPermission()) {
            return
        }

        val hasSession = sessionRestoreManager.restoreIfRequired()

        if (hasSession) {
            scheduler.startWatchdog()
        }
    }

}