package com.ecosphere.partner.feature.tracking.offline.model

import com.ecosphere.partner.feature.tracking.model.TrackingPoint

data class QueuedTrackingPoint(

    val id: Long,

    val point: TrackingPoint

)
data class OfflineBatch(

    val items: List<QueuedTrackingPoint>

)