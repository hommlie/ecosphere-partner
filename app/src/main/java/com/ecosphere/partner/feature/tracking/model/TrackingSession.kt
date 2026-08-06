package com.ecosphere.partner.feature.tracking.model

data class TrackingSession(
    val sessionId: String,

    val userId: String,
    val driverName : String,

    val vehicleNumber : String,

    val startTime: Long,
    val endTime: Long? = null,

    val totalDistance: Float = 0f,
    val totalPoints: Int = 0,

    val status: TrackingStatus
)
