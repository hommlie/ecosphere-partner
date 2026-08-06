package com.ecosphere.partner.feature.tracking.location

import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import kotlinx.coroutines.flow.Flow

interface LocationProvider {

    fun locationUpdates(): Flow<TrackingPoint>

    fun stop()
}