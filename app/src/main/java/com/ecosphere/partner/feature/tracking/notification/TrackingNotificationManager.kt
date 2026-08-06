package com.ecosphere.partner.feature.tracking.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ecosphere.partner.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.ecosphere.partner.feature.tracking.service.LocationTrackingService
import java.util.concurrent.TimeUnit

@Singleton
class TrackingNotificationManager @Inject constructor(

    @ApplicationContext
    private val context: Context

) {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context)
    }

    fun createChannel() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            NotificationConstants.CHANNEL_ID,
            NotificationConstants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {

            description = NotificationConstants.CHANNEL_DESCRIPTION

            setShowBadge(false)
            enableLights(false)
            enableVibration(false)

            lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        }

        val manager =
            context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(channel)

    }

    fun buildNotification(
        distance: Float,
        duration: Long
    ): Notification {

        return builder(
            distance = distance,
            duration = duration
        ).build()
    }

    //@SuppressLint("MissingPermission")
    fun updateNotification(
        distance: Float,
        duration: Long
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        notificationManager.notify(
            NotificationConstants.NOTIFICATION_ID,
            builder(distance, duration).build()
        )
    }

    fun cancelNotification() {

        notificationManager.cancel(
            NotificationConstants.NOTIFICATION_ID
        )

    }

    private fun builder(
        distance: Float,
        duration: Long
    ): NotificationCompat.Builder {

        return NotificationCompat.Builder(
            context,
            NotificationConstants.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tracking Active")
            .setContentText(
                "Distance ${
                    String.format("%.2f", distance / 1000f)
                } km"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        """
                            
Tracking is running
Distance : ${
                            String.format("%.2f", distance / 1000f)
                        } km
Duration : ${formatDuration(duration)}""".trimIndent()
                    )
            )
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setOngoing(true)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
//            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent())

    }

    private fun stopPendingIntent() =
        android.app.PendingIntent.getService(
            context,
            100,
            Intent(
                context,
                LocationTrackingService::class.java
            ).apply {

                action = NotificationConstants.ACTION_STOP

            },
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

    private fun formatDuration(
        duration: Long
    ): String {

        val hours =
            TimeUnit.MILLISECONDS.toHours(duration)

        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(duration) % 60

        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(duration) % 60

        return String.format(
            "%02d:%02d:%02d",
            hours,
            minutes,
            seconds
        )

    }

}