package com.ecosphere.partner.feature.tracking.model

sealed interface TrackingState {
    data object Idle : TrackingState

    data object Starting : TrackingState

    data class Tracking(
        val sessionId: String,
        val startedAt: Long,
        val totalDistance: Float,
        val totalPoints: Int
    ) : TrackingState

    data object Stopping : TrackingState

    data class Stopped(
        val sessionId: String,
        val startedAt: Long,
        val endedAt: Long,
        val totalDistance: Float,
        val totalPoints: Int
    ) : TrackingState

    data class Error(
        val throwable: Throwable
    ) : TrackingState
}