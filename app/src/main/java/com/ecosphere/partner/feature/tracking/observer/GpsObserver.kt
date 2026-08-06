package com.ecosphere.partner.feature.tracking.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GpsObserver @Inject constructor(@ApplicationContext private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _state = MutableStateFlow(currentState())

    val state: StateFlow<GpsState> = _state.asStateFlow()

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {

                _state.value = currentState()
            }
        }
    }

    fun start() {

        if (isRegistered) return

        val filter = IntentFilter(
            LocationManager.PROVIDERS_CHANGED_ACTION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            context.registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                receiver,
                filter
            )
        }

        isRegistered = true
        _state.value = currentState()
    }

    fun stop() {

        if (!isRegistered) return

        context.unregisterReceiver(receiver)

        isRegistered = false

    }

    fun isGpsEnabled(): Boolean {

        return locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) ||
                locationManager.isProviderEnabled(
                    LocationManager.NETWORK_PROVIDER
                )
    }

    private fun currentState(): GpsState {

        return if (isGpsEnabled()) {

            GpsState.Enabled
        } else {
            GpsState.Disabled
        }
    }

}