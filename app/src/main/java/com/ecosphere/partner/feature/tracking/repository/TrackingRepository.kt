package com.ecosphere.partner.feature.tracking.repository

import com.ecosphere.partner.feature.tracking.model.LiveTracking
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import kotlinx.coroutines.flow.Flow

interface TrackingRepository {

    /**
     * Creates a new tracking session.
     */
    suspend fun startSession(
        session: TrackingSession
    )

    /**
     * Saves a single GPS point.
     */
    suspend fun savePoint(
        point: TrackingPoint
    )

    /**
     * Updates running statistics.
     * Can be called frequently while tracking.
     */
    suspend fun updateSession(
        totalDistance: Float,
        totalPoints: Int
    )

    /**
     * Marks session completed.
     */
    suspend fun finishSession(
        endTime: Long,
        totalDistance: Float,
        totalPoints: Int
    )

    suspend fun updateLiveTracking(liveTracking : LiveTracking)

    /**
     * Returns current active session.
     */
    suspend fun getCurrentSession(): TrackingSession?

    /**
     * Emits current session updates.
     * Useful for future UI, notification,
     * widgets and API synchronization.
     */
    fun observeCurrentSession(): Flow<TrackingSession?>

    /**
     * Clears in-memory session.
     * Does NOT delete Firebase data.
     */
    suspend fun clearSession()

    suspend fun restoreSession(session: TrackingSession)
}