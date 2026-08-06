package com.ecosphere.partner.feature.tracking.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.manager.TrackingManager
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import com.ecosphere.partner.feature.tracking.service.LocationTrackingService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TrackingWatchdogWorker @AssistedInject constructor(

    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,

    private val sessionRestoreManager: SessionRestoreManager,
    private val trackingManager: TrackingManager,
    private val permissionManager: PermissionManager

) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {

        Log.d("WATCHDOG", "TrackingWatchdogWorker started")


        return try {

            Log.d("WATCHDOG", "Worker dependencies injected successfully")

            if (!sessionRestoreManager.hasActiveSession()) {
                return Result.success()
            }

            if (!permissionManager.hasLocationPermission()) {
                return Result.success()
            }

            if (!trackingManager.isTracking()) {
                LocationTrackingService.start(applicationContext)
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("WATCHDOG", "Worker failed", e)
            Result.retry()
        }
    }
}