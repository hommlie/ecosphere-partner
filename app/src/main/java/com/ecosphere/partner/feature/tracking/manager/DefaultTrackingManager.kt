package com.ecosphere.partner.feature.tracking.manager

import com.ecosphere.partner.feature.tracking.engine.DefaultTrackingEngine
import com.ecosphere.partner.feature.tracking.model.TrackingRuntime
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class DefaultTrackingManager @Inject constructor(
    private val engineProvider: Provider<DefaultTrackingEngine>
) : TrackingManager {

    private var engine: DefaultTrackingEngine? = null

//    override val runtime: StateFlow<TrackingRuntime?>
//        get() = requireNotNull(engine) {
//            "Tracking has not been started."
//        }.runtimeState

    private val managerScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )
    private var runtimeJob: Job? = null
    private val _runtime = MutableStateFlow<TrackingRuntime?>(null)

    override val runtime: StateFlow<TrackingRuntime?>
        get() = _runtime.asStateFlow()

    override fun start(session: TrackingSession) {

        if (engine != null) return

        val trackingEngine = engineProvider.get()

        engine = trackingEngine

        runtimeJob?.cancel()
        runtimeJob = managerScope.launch {
            trackingEngine.runtimeState.collect {
                _runtime.value = it
            }
        }
        trackingEngine.start(session)
    }

    override suspend fun stop() {

        val trackingEngine = engine ?: return

        try {
            trackingEngine.stop()
        } finally {
            runtimeJob?.cancel()
            runtimeJob = null

            _runtime.value = null

            trackingEngine.release()
            engine = null
        }
    }

    override fun isTracking(): Boolean {
        return engine != null
    }
    override fun resumeIfRequired() {
        engine?.restartLocationUpdates()
    }
}