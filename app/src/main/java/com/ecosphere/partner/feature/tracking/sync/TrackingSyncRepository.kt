package com.ecosphere.partner.feature.tracking.sync

import com.ecosphere.partner.feature.tracking.model.TrackingPoint

interface TrackingSyncRepository {

    suspend fun uploadPoint(
        sessionId: String,
        point: TrackingPoint
    )
}