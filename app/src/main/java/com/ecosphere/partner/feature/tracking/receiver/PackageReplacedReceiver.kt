package com.ecosphere.partner.feature.tracking.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import com.ecosphere.partner.feature.tracking.repository.TrackingRepository
import com.ecosphere.partner.feature.tracking.scheduler.TrackingScheduler
import com.ecosphere.partner.feature.tracking.service.LocationTrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageReplacedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TrackingRepository
    @Inject
    lateinit var sessionRestoreManager: SessionRestoreManager
    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var scheduler: TrackingScheduler

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {

            if (!sessionRestoreManager.hasActiveSession()) return@launch
            Log.d("Package Replaced", "Has Session = ${sessionRestoreManager.hasActiveSession()}")

            if (!permissionManager.hasLocationPermission()) return@launch


            Log.d("Package Replaced", "Starting Service")
            scheduler.startWatchdog()
            LocationTrackingService.start(context)

        }
    }
}