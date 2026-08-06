package com.ecosphere.partner.feature.tracking.sync

import com.google.firebase.database.DatabaseReference
import com.ecosphere.partner.feature.tracking.model.TrackingPoint
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FirebaseTrackingSyncRepository @Inject constructor(
    private val database: DatabaseReference,
    @Named("tracking_sessions")
    private val trackingCollection: CollectionReference
) : TrackingSyncRepository {

    override suspend fun uploadPoint(

        sessionId: String,

        point: TrackingPoint

    ) {

//        database
//            .child("tracking")
//            .child(sessionId)
//            .child("points")
//            .push()
//            .setValue(point)
//            .await()

        trackingCollection
            .document(sessionId)
            .collection("points")
            .add(point)
            .await()

    }

}