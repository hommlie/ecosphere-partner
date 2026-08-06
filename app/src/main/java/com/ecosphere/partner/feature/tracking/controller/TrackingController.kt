package com.ecosphere.partner.feature.tracking.controller

import android.content.Context
import android.util.Log
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.manager.TrackingManager
import com.ecosphere.partner.feature.tracking.model.TrackingRuntime
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import com.ecosphere.partner.feature.tracking.scheduler.TrackingScheduler
import com.ecosphere.partner.feature.tracking.service.LocationTrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingController @Inject constructor(

    @ApplicationContext
    private val context: Context,

    private val trackingManager: TrackingManager,
    private val permissionManager: PermissionManager,
    private val scheduler: TrackingScheduler,
    private val sessionRestoreManager: SessionRestoreManager

) {

    private val trackingControllerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val runtime: StateFlow<TrackingRuntime?>
        get() = trackingManager.runtime

    fun start() {

        if (trackingManager.isTracking()) {
            return
        }
        if (!permissionManager.hasLocationPermission()) {
            return
        }

        scheduler.startWatchdog()
        LocationTrackingService.start(context)
    }

    suspend fun stop() {

        if (!trackingManager.isTracking()) {
            return
        }
        trackingManager.stop()
        scheduler.stopWatchdog()
        LocationTrackingService.stop(context)
    }

    fun isTracking(): Boolean {
        return trackingManager.isTracking()
    }
    fun onLocationPermissionGranted() {
        Log.d("PERMISSION Checkup", "engineTracking = ${trackingManager.isTracking()}")
        trackingControllerScope.launch {

            if (!sessionRestoreManager.hasActiveSession()) return@launch
            if (trackingManager.isTracking()) {
                trackingManager.resumeIfRequired()
                return@launch
            }
            start()
        }
    }
}