package com.ecosphere.partner.feature.tracking.manager

import com.ecosphere.partner.feature.tracking.model.TrackingRuntime
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import kotlinx.coroutines.flow.StateFlow

interface TrackingManager {

    fun start(session: TrackingSession)
    suspend fun stop()
    fun isTracking(): Boolean
    fun resumeIfRequired ()
    val runtime: StateFlow<TrackingRuntime?>

}