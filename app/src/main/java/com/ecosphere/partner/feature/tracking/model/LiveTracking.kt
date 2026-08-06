package com.ecosphere.partner.feature.tracking.model

data class LiveTracking(

    val sessionId: String = "",

    val driverId: String = "",
    val driverName: String = "",

    val vehicleNumber: String = "",

    val startTime: Long = 0L,
    val endTime: Long = 0L,

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val bearing: Float = 0f,
    val speed: Float = 0f,
    val accuracy: Float = 0f,
    val altitude: Double = 0.0,

    val totalDistance: Float = 0f,

    val timestamp: Long = 0L,

    val status: String = TrackingStatus.STARTED.name,
)
