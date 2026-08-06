package com.ecosphere.partner.feature.tracking.repository

import com.ecosphere.partner.feature.tracking.model.LiveTracking
import com.google.firebase.database.DatabaseReference
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import com.ecosphere.partner.feature.tracking.model.TrackingStatus
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FirebaseTrackingRepository @Inject constructor(
    private val trackingReference: DatabaseReference,
    @Named("tracking_sessions")
    private val trackingCollection: CollectionReference
) : TrackingRepository {

    private val currentSession = MutableStateFlow<TrackingSession?>(null)

    override suspend fun startSession(
        session: TrackingSession
    ) {

//        trackingReference
//            .child(session.userId)
//            .child(session.sessionId)
//            .child(METADATA)
//            .setValue(session)
//            .await()

        trackingCollection
            .document(session.sessionId)
            .set(session)
            .await()

        currentSession.value = session
    }

    override suspend fun savePoint(
        point: TrackingPoint
    ) {

        val session = currentSession.value ?: return

//        trackingReference
//            .child(session.userId)
//            .child(session.sessionId)
//            .child(POINTS)
//            .push()
//            .setValue(point)
//            .await()

        trackingCollection
            .document(session.sessionId)
            .collection(POINTS)
            .add(point)
            .await()
    }

    override suspend fun updateSession(
        totalDistance: Float,
        totalPoints: Int
    ) {

        val session = currentSession.value ?: return

        val updatedSession = session.copy(
            totalDistance = totalDistance,
            totalPoints = totalPoints
        )

//        trackingReference
//            .child(session.userId)
//            .child(session.sessionId)
//            .child(METADATA)
//            .setValue(updatedSession)
//            .await()

        trackingCollection
            .document(session.sessionId)
            .update(
                mapOf(
                    "totalDistance" to totalDistance,
                    "totalPoints" to totalPoints
                )
            )
            .await()

        currentSession.value = updatedSession
    }

    override suspend fun finishSession(
        endTime: Long,
        totalDistance: Float,
        totalPoints: Int
    ) {

        val session = currentSession.value ?: return

        val completedSession = session.copy(
            endTime = endTime,
            totalDistance = totalDistance,
            totalPoints = totalPoints,
            status = TrackingStatus.STOPPED
        )

        trackingReference
            .child(session.userId)
            .updateChildren(
                mapOf(
                "endTime" to endTime,
                "status" to TrackingStatus.STOPPED.name
            ))
            .await()

        trackingCollection
            .document(session.sessionId)
            .update(
                mapOf(
                    "endTime" to endTime,
                    "status" to TrackingStatus.STOPPED.name,
                    "totalDistance" to totalDistance,
                    "totalPoints" to totalPoints
                )
            )
            .await()

        currentSession.value = completedSession
    }

    override suspend fun updateLiveTracking(
        liveTracking: LiveTracking
    ) {
        trackingReference
            .child(liveTracking.driverId)
            .setValue(liveTracking)
            .await()
    }

    override suspend fun getCurrentSession(): TrackingSession? {
        return currentSession.value
    }

    override fun observeCurrentSession(): Flow<TrackingSession?> {
        return currentSession.asStateFlow()
    }

    override suspend fun clearSession() {
        currentSession.emit(null)
    }

    override suspend fun restoreSession(session: TrackingSession) {
        currentSession.value = session
    }

    private companion object {

        const val METADATA = "metadata"
        const val POINTS = "points"
    }
}