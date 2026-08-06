package com.ecosphere.partner.feature.tracking.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import com.ecosphere.partner.feature.tracking.scheduler.TrackingScheduler
import com.ecosphere.partner.feature.tracking.service.LocationTrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionRestoreManager: SessionRestoreManager

    @Inject
    lateinit var scheduler: TrackingScheduler
    @Inject
    lateinit var permissionManager: PermissionManager

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Log.d("BootReceiver", "Action = ${intent.action}")

        when (intent.action) {

            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {

                Log.d("BootReceiver", "Boot received")

                CoroutineScope(Dispatchers.IO).launch {

                    if (!sessionRestoreManager.hasActiveSession()) return@launch
                    Log.d("BootReceiver", "Has Session = ${sessionRestoreManager.hasActiveSession()}")

                    if (!permissionManager.hasLocationPermission()) return@launch

                    Log.d("BootReceiver", "Starting Service")

                    scheduler.startWatchdog()
                    LocationTrackingService.start(context)

                }
            }
        }
    }
}