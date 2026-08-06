package com.ecosphere.partner.feature.tracking.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<TrackingPoint> = callbackFlow {

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        )
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .setWaitForAccurateLocation(true)
            .build()

        Log.d("LOCATION check", "Location Callback")
        callback = object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {

                result.locations.forEach { location ->

                    if (!location.isValid()) return@forEach

                    trySend(location.toTrackingPoint())
                }
            }
        }

        Log.d("LOCATION check", "Request Location Updates")
        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                callback!!,
                Looper.getMainLooper()
            )
            Log.d("LOCATION check", "requestLocationUpdates registered")
        } catch (e: SecurityException) {
            Log.e("LOCATION check", "Permission missing", e)
            close(e)
        }

        awaitClose {
            Log.d("LOCATION check", "Location Flow Closed")
            callback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            callback = null
        }
    }

    override fun stop() {

        callback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }

        callback = null
    }

    private fun Location.isValid(): Boolean {

        if (!hasAccuracy()) return false

        if (accuracy > MAX_ALLOWED_ACCURACY) return false

        return true
    }

    private fun Location.toTrackingPoint() = TrackingPoint(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        speed = speed,
        bearing = bearing,
        altitude = altitude,
        timestamp = time
    )

    companion object {

        private const val UPDATE_INTERVAL = 10_000L
        private const val MIN_UPDATE_INTERVAL = 5_000L
        private const val MIN_DISTANCE_METERS = 5f
        private const val MAX_ALLOWED_ACCURACY = 30f
    }
}