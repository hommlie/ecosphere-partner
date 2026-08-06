package com.ecosphere.partner.feature.tracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.feature.permissions.manager.PermissionManager
import com.ecosphere.partner.feature.tracking.manager.TrackingManager
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import com.ecosphere.partner.feature.tracking.model.TrackingStatus
import com.ecosphere.partner.feature.tracking.notification.NotificationConstants
import com.ecosphere.partner.feature.tracking.notification.TrackingNotificationManager
import com.ecosphere.partner.feature.tracking.recovery.SessionRestoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var trackingManager: TrackingManager

    @Inject
    lateinit var notificationManager: TrackingNotificationManager

    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var sessionRestoreManager: SessionRestoreManager
    @Inject
    lateinit var permissionManager: PermissionManager

    private var restoredSession: TrackingSession? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    override fun onCreate() {
        super.onCreate()
        Log.d("TrackingService", "onCreate")
        notificationManager.createChannel()

        serviceScope.launch {
            restoredSession = sessionManager.getTrackingSession()
        }
    }

  override fun onStartCommand(
      intent: Intent?,
      flags: Int,
      startId: Int
  ): Int {

      Log.d("TrackingService", "Action = ${intent?.action}")

      return when (intent?.action) {

          ACTION_START -> {

              if (!permissionManager.hasLocationPermission()) {
                  Log.w("TrackingService", "Location permission missing.")
                  stopSelf()
                  START_NOT_STICKY
              } else {

                  val session = restoredSession

                  startForeground(
                      NotificationConstants.NOTIFICATION_ID,
                      notificationManager.buildNotification(
                          distance = session?.totalDistance ?: 0f,
                          duration = session?.let {
                              System.currentTimeMillis() - it.startTime
                          } ?: 0L
                      )
                  )

                  if (!trackingManager.isTracking()) {
                      serviceScope.launch {
                          startTracking()
                      }
                  }

                  START_STICKY
              }
          }

          ACTION_STOP,
          NotificationConstants.ACTION_STOP -> {

              serviceScope.launch {
                  stopTracking()
              }

              START_NOT_STICKY
          }

          else -> START_NOT_STICKY
      }
  }

    private suspend fun startTracking() {

        if (trackingManager.isTracking()) return

        if (sessionRestoreManager.restoreIfRequired()) {
            val session = sessionManager.getTrackingSession()

            Log.d("RESTORE", "Session From DataStore = $session")
            return
        }

        val userId =
            sessionManager
                .getSession()
                .driverId
                ?.takeIf { it.isNotBlank() }
                ?: run {
                    stopSelf()
                    return
                }

        val session = TrackingSession(
            sessionId = UUID.randomUUID().toString(),
            userId = userId,
            driverName = sessionManager.getSession().driverName ?: "",
            vehicleNumber = sessionManager.getSession().vehicleNumber ?: "",
            startTime = System.currentTimeMillis(),
            status = TrackingStatus.STARTED
        )

        sessionManager.saveTrackingSession(session)

        trackingManager.start(session)
    }
    private fun stopTracking() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    companion object {

        private const val ACTION_START = "tracking.action.START"
        private const val ACTION_STOP = "tracking.action.STOP"

        fun start(context: Context) {

            val intent = Intent(context, LocationTrackingService::class.java
            ).apply {
                action = ACTION_START
            }

            ContextCompat.startForegroundService(
                context,
                intent
            )
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java
            ).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}