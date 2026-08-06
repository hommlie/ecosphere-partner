package com.ecosphere.partner.feature.tracking.recovery

import android.util.Log
import com.ecosphere.partner.core.datastore.SessionManager
import com.ecosphere.partner.feature.tracking.manager.TrackingManager
import com.ecosphere.partner.feature.tracking.model.TrackingStatus
import com.ecosphere.partner.feature.tracking.repository.TrackingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRestoreManager @Inject constructor(

    private val repository: TrackingRepository,
    private val trackingManager: TrackingManager,
    private val sessionManager: SessionManager

) {

    suspend fun restoreIfRequired(): Boolean {

        if (!sessionManager.isOnDuty()) {
            sessionManager.clearTrackingSession()
            repository.clearSession()
            return false
        }

        if (trackingManager.isTracking()) {
            return true
        }

        val session = sessionManager.getTrackingSession()
            ?: return false
        Log.d("RESTORE", "Restored Session = $session")

        if (session.status != TrackingStatus.STARTED) {

            sessionManager.clearTrackingSession()
            repository.clearSession()

            return false
        }

        repository.restoreSession(session)
        trackingManager.start(session)

        return true

    }

    suspend fun hasActiveSession(): Boolean {

        if (!sessionManager.isOnDuty()) {
            sessionManager.clearTrackingSession()
            repository.clearSession()
            return false
        }

        val session = sessionManager.getTrackingSession()
            ?: return false
        Log.d("RESTORE", "Restored Session = $session")
        // repository.getCurrentSession()

        return session?.status == TrackingStatus.STARTED

    }

    suspend fun clearInvalidSession() {

//        val session = repository.getCurrentSession()
//                ?: return

        val session = sessionManager.getTrackingSession()
                ?: return

        if (session.status != TrackingStatus.STARTED) {

            sessionManager.clearTrackingSession()
            repository.clearSession()
        }
    }

}