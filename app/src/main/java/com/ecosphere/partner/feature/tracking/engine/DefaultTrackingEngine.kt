package com.ecosphere.partner.feature.tracking.engine

import android.util.Log
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.feature.tracking.location.LocationProvider
import com.ecosphere.partner.feature.tracking.model.LiveTracking
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.model.TrackingRuntime
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import com.ecosphere.partner.feature.tracking.notification.TrackingNotificationManager
import com.ecosphere.partner.feature.tracking.observer.GpsObserver
import com.ecosphere.partner.feature.tracking.observer.GpsState
import com.ecosphere.partner.feature.tracking.repository.TrackingRepository
import com.ecosphere.partner.feature.tracking.utils.DistanceCalculator
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class DefaultTrackingEngine @Inject constructor(

    private val repository: TrackingRepository,
    private val locationProvider: LocationProvider,
    private val distanceCalculator: DistanceCalculator,
    private val notificationManager: TrackingNotificationManager,
    private val gpsObserver: GpsObserver,
    private val sessionManager: SessionManager
) {

    private var gpsJob: Job? = null
    private var locationJob: Job? = null
    private var metadataJob: Job? = null
    private var liveTrackingJob: Job? = null

    private var lastNotificationUpdate = 0L

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        }

    private val engineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + exceptionHandler
    )

    private val runtime = MutableStateFlow<TrackingRuntime?>(null)

    val runtimeState: StateFlow<TrackingRuntime?> = runtime.asStateFlow()

    fun start(
        session: TrackingSession
    ) {

        Log.d("ENGINE check ", "sessionDistance=${session.totalDistance}, sessionPoints=${session.totalPoints}")
        if (runtime.value != null) return

        Log.d("ENGINE check", "sessionDistance=${session.totalDistance}, sessionPoints=${session.totalPoints}")

        runtime.value = TrackingRuntime(
            session = session,
            totalDistance = session.totalDistance,
            totalPoints = session.totalPoints
        )
        startInternal(session)
    }

    suspend fun stop() {
        stopInternal()
    }

    fun release() {
        engineScope.cancel()
    }

    private fun startInternal(
        session: TrackingSession
    ) {

        locationJob?.cancel()
        metadataJob?.cancel()
        gpsJob?.cancel()
        liveTrackingJob?.cancel()

        gpsObserver.start()

        observeGps()

        locationJob = engineScope.launch {
            try {
                Log.d("ENGINE check", "Location Job Started")
                repository.startSession(session)

                locationProvider
                    .locationUpdates()
                    .collectLatest { point ->
                        Log.d("ENGINE check", "Location Collected")
                        processLocation(point)
                    }

                Log.d("ENGINE check", "Location Flow Completed")

            }catch (e : Exception){
                Log.d("ENGINE check", "Error = ${e.message}")
                Log.e("ENGINE check", "Location Job Failed", e)
            }
        }
        startMetadataUpdater()
        startLiveTrackingUpdater()
    }

    private suspend fun processLocation(
        point: TrackingPoint
    ) {

        val currentRuntime = runtime.value ?: return

        val updatedRuntime =
            updateRuntime(
                currentRuntime,
                point
            )

        persistPoint(point)

        updateTrackingNotification(
            updatedRuntime
        )
    }

    private fun updateRuntime(
        currentRuntime: TrackingRuntime,
        point: TrackingPoint
    ): TrackingRuntime {

        val distance =
            distanceCalculator.calculate(
                currentRuntime.previousPoint,
                point
            )

        val updatedRuntime =
            currentRuntime.update(
                point = point,
                distance = distance
            )

        runtime.value = updatedRuntime

        return updatedRuntime
    }

    private suspend fun persistPoint(
        point: TrackingPoint
    ) {

        try {
            repository.savePoint(point)

        } catch (_: Exception) {
        // TODO
        // Offline Queue
        }
    }

    private fun updateTrackingNotification(
        runtime: TrackingRuntime
    ) {

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastNotificationUpdate < 30_000) {
            return
        }

        lastNotificationUpdate = currentTime

        notificationManager.updateNotification(
            distance = runtime.totalDistance,
            duration = currentTime - runtime.session.startTime
        )

    }
    private fun startMetadataUpdater() {

        metadataJob?.cancel()

        metadataJob = engineScope.launch {

            while (isActive) {

                delay(60_000)

                val state = runtime.value ?: continue

                repository.updateSession(
                    totalDistance = state.totalDistance,
                    totalPoints = state.totalPoints
                )
                val updatedSession = state.session.copy(
                    totalDistance = state.totalDistance,
                    totalPoints = state.totalPoints
                )
                sessionManager.saveTrackingSession(updatedSession)
            }
        }
    }

    private fun startLiveTrackingUpdater() {

        liveTrackingJob?.cancel()

        liveTrackingJob = engineScope.launch {

            while (isActive) {

                delay(15_000)

                val state = runtime.value ?: continue
                val point = state.previousPoint ?: continue

                repository.updateLiveTracking(
                    LiveTracking(
                        sessionId = state.session.sessionId,
                        driverId = state.session.userId,
                        driverName = state.session.driverName,
                        vehicleNumber = state.session.vehicleNumber,

                        startTime = state.session.startTime,

                        latitude = point.latitude,
                        longitude = point.longitude,

                        bearing = point.bearing,
                        speed = point.speed,
                        accuracy = point.accuracy,
                        altitude = point.altitude,

                        timestamp = point.timestamp,

                        status = state.session.status.name
                    )
                )
            }
        }
    }

    private suspend fun stopInternal() {

        val state = runtime.value ?: return

        try {
            repository.updateSession(
                    totalDistance = state.totalDistance,
                    totalPoints = state.totalPoints
                )
            val updatedSession = state.session.copy(
                    totalDistance = state.totalDistance,
                    totalPoints = state.totalPoints
            )
            sessionManager.saveTrackingSession(updatedSession)

            repository.finishSession(
                endTime = System.currentTimeMillis(),
                totalDistance = state.totalDistance,
                totalPoints = state.totalPoints
            )

            repository.clearSession()
            sessionManager.clearTrackingSession()

        } finally {
            cleanup()
            runtime.value = null
            lastNotificationUpdate = 0L
            notificationManager.cancelNotification()
        }
    }
    fun restartLocationUpdates() {


        // Tracking session hi nahi hai
        if (runtime.value == null) return

        // Already collecting
        if (locationJob?.isActive == true) return

        locationProvider.stop()

        locationJob = engineScope.launch {
            locationProvider.locationUpdates()
                .collectLatest(::processLocation)
        }
    }

    private fun cleanup() {

        gpsObserver.stop()

        locationProvider.stop()

        gpsJob?.cancel()
        locationJob?.cancel()
        metadataJob?.cancel()
        liveTrackingJob?.cancel()

        gpsJob = null
        locationJob = null
        metadataJob = null
        liveTrackingJob = null

    }

    private fun observeGps() {

        gpsJob?.cancel()

        gpsJob = engineScope.launch {

            gpsObserver.state.collect { state ->

                when (state) {

                    GpsState.Enabled -> {
                        // Future: Resume tracking if required
                    }

                    GpsState.Disabled -> {
                        // Future: Show notification / log event
                    }
                }
            }
        }
    }

    private fun handleError(
        throwable: Throwable
    ) {

        if (throwable is CancellationException) {
            return
        }
    // TODO
        // Add Crashlytics / Timber logging
    }

}