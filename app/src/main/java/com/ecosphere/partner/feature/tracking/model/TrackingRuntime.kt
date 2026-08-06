package com.ecosphere.partner.feature.tracking.model

data class TrackingRuntime(

    val session: TrackingSession,

    val previousPoint: TrackingPoint? = null,

    val totalDistance: Float = 0f,

    val totalPoints: Int = 0,

    val startedAt: Long = session.startTime,

    val currentSpeed: Float = 0f
) {

    fun update(
        point: TrackingPoint,
        distance: Float
    ): TrackingRuntime {

        return copy(

            previousPoint = point,
            totalDistance = totalDistance + distance,
            totalPoints = totalPoints + 1,
            currentSpeed = point.speed
        )
    }
}