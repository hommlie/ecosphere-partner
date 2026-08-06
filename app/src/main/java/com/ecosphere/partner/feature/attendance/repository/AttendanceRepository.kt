package com.ecosphere.partner.feature.attendance.repository

import android.util.Log
import com.ecosphere.partner.core.network.SafeFirestoreCall
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import com.ecosphere.partner.feature.attendance.model.TrackingSessionUi
import jakarta.inject.Inject
import java.util.Calendar
import javax.inject.Named

class AttendanceRepository @Inject constructor(
    @Named("tracking_sessions")
    private val trackingCollection: CollectionReference,
    private val safeFirestoreCall: SafeFirestoreCall
) {

    suspend fun getTrackingSessions(
        userId: String,
        date: Long
    ) = safeFirestoreCall {

        val (start, end) = getDayRange(date)

        Log.d("Firestore", "userId = $userId")
        Log.d("Firestore", "start = $start")
        Log.d("Firestore", "end = $end")

        trackingCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startTime", start)
            .whereLessThan("startTime", end)
            .orderBy("startTime")
            .get()
            .await()
            .toObjects(TrackingSessionUi::class.java)
    }

    suspend fun getTrackingPoints(
        sessionId: String
    ) = safeFirestoreCall {

        val points = mutableListOf<TrackingPoint>()

        var lastDocument: DocumentSnapshot? = null

        while (true) {

            val snapshot = fetchPage(
                sessionId,
                lastDocument
            )

            if (snapshot.isEmpty)
                break

            points += snapshot.toObjects(
                TrackingPoint::class.java
            )

            lastDocument = snapshot.documents.last()

            if (snapshot.size() < PAGE_SIZE)
                break
        }

        points
            .distinctBy {
                Triple(
                    it.latitude,
                    it.longitude,
                    it.timestamp
                )
            }
    }

    private suspend fun fetchPage(
        sessionId: String,
        lastDocument: DocumentSnapshot?
    ): QuerySnapshot {

        var query =
            trackingCollection
                .document(sessionId)
                .collection(POINTS_COLLECTION)
                .orderBy("timestamp")
                .limit(PAGE_SIZE)

        if (lastDocument != null) {
            query =
                query.startAfter(lastDocument)
        }

        return query
            .get()
            .await()
    }

    private fun getDayRange(
        date: Long
    ): Pair<Long, Long> {

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = date

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)

        val endOfDay = calendar.timeInMillis

        return startOfDay to endOfDay
    }

    private companion object {
        const val PAGE_SIZE = 1000L
        const val POINTS_COLLECTION = "points"
    }
}