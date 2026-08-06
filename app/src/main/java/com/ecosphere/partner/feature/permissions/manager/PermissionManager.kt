package com.ecosphere.partner.feature.permissions.manager

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.ecosphere.partner.feature.permissions.model.PermissionStatus
import com.ecosphere.partner.feature.permissions.model.PermissionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val permissionOrder = listOf(

        PermissionType.LOCATION,
        PermissionType.BACKGROUND_LOCATION,
        PermissionType.NOTIFICATION,
        PermissionType.BATTERY_OPTIMIZATION,
        PermissionType.GPS
    )

    /**
     * Returns true if every required permission is granted.
     */
    fun hasAllRequiredPermissions(): Boolean =
        getMissingPermissions().isEmpty()

    /**
     * Returns all permissions that are still missing.
     * Already sorted in business order.
     */
    fun getMissingPermissions(): List<PermissionType> =
        PermissionType.entries.filter { permission ->
            when (getPermissionStatus(permission)) {
                PermissionStatus.GRANTED,
                PermissionStatus.NOT_REQUIRED -> false

                PermissionStatus.DENIED,
                PermissionStatus.PERMANENTLY_DENIED -> true
            }
        }

    /**
     * Returns current status of a permission.
     *
     * NOTE:
     * PERMANENTLY_DENIED is NOT determined here.
     * Activity decides that using
     * shouldShowRequestPermissionRationale().
     */
    fun getPermissionStatus(
        type: PermissionType
    ): PermissionStatus {

        return when (type) {

            PermissionType.LOCATION -> {
                if (hasLocationPermission())
                    PermissionStatus.GRANTED
                else
                    PermissionStatus.DENIED
            }

            PermissionType.BACKGROUND_LOCATION -> {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    PermissionStatus.NOT_REQUIRED
                } else {
                    if (hasBackgroundLocationPermission())
                        PermissionStatus.GRANTED
                    else
                        PermissionStatus.DENIED
                }
            }

            PermissionType.NOTIFICATION -> {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    PermissionStatus.NOT_REQUIRED
                } else {
                    if (hasNotificationPermission())
                        PermissionStatus.GRANTED
                    else
                        PermissionStatus.DENIED
                }
            }

            PermissionType.BATTERY_OPTIMIZATION -> {

                if (isIgnoringBatteryOptimization())
                    PermissionStatus.GRANTED
                else
                    PermissionStatus.DENIED
            }

            PermissionType.GPS -> {

                if (isGpsEnabled()) {
                    PermissionStatus.GRANTED
                } else {
                    PermissionStatus.DENIED
                }
            }
        }
    }

    /**
     * Returns true when device location service is enabled.
     */
    fun isGpsEnabled(): Boolean {

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
     * Returns true if app is ignoring battery optimization.
     */
    fun isIgnoringBatteryOptimization(): Boolean {

        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return powerManager.isIgnoringBatteryOptimizations(
            context.packageName
        )
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasBackgroundLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasNotificationPermission(): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns the first missing mandatory permission.
     */
    fun getCurrentMissingPermission(): PermissionType? {

        return permissionOrder.firstOrNull { permission ->

            getPermissionStatus(permission) != PermissionStatus.GRANTED &&
                    getPermissionStatus(permission) != PermissionStatus.NOT_REQUIRED
        }
    }
    fun getPermissionOrder(): List<PermissionType> = permissionOrder
}