package com.ecosphere.partner.core.datastore

import com.google.gson.Gson
import com.ecosphere.partner.core.network.TokenStore
import com.ecosphere.partner.feature.tracking.model.TrackingSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles authenticated user session.
 *
 * Responsibility:
 * - Save session information.
 * - Read session information.
 * - Clear session on logout.
 *
 * Business layer should always use this class instead of accessing
 * PreferenceManager directly.
 */

@Singleton
class SessionManager @Inject constructor(
    private val preferenceManager: PreferenceManager,
//    private val tokenStore: TokenStore
) {

    private val gson = Gson()

    suspend fun saveAccessToken(token: String) {
        preferenceManager.saveString(
            PreferenceKeys.ACCESS_TOKEN,
            token
        )
    }
    suspend fun getAccessToken(): String? {
        return preferenceManager
            .getString(PreferenceKeys.ACCESS_TOKEN)
            .first()
    }

    suspend fun saveDriverId(partnerId: String) {
        preferenceManager.saveString(
            PreferenceKeys.DRIVER_ID,
            partnerId
        )
    }

    suspend fun getDriverId(): String? {
        return preferenceManager
            .getString(PreferenceKeys.DRIVER_ID)
            .first()
    }

    suspend fun saveDriverName(name: String) {
        preferenceManager.saveString(
            PreferenceKeys.DRIVER_NAME,
            name
        )
    }
    suspend fun getDriverName(): String? {
        return preferenceManager
            .getString(PreferenceKeys.DRIVER_NAME)
            .first()
    }

    suspend fun getVehicleNumber(): String? {
        return preferenceManager
            .getString(PreferenceKeys.VEHICLE_NUMBER)
            .first()
    }
    suspend fun getDriverProfile(): String? {
        return preferenceManager
            .getString(PreferenceKeys.DRIVER_PROFILE)
            .first()
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        preferenceManager.saveBoolean(
            PreferenceKeys.IS_LOGGED_IN,
            isLoggedIn
        )
    }

    suspend fun isLoggedIn(): Boolean {
        return preferenceManager
            .getBoolean(PreferenceKeys.IS_LOGGED_IN)
            .first()
    }

    /**
     * Save complete authenticated session.
     * Edit Used for single write operations.
     * If app crash everything will save or nothing will be saved.
     */
    suspend fun saveSession(
        session: UserSession
    ) {

        preferenceManager.edit {

            session.accessToken?.let {
                this[PreferenceKeys.ACCESS_TOKEN] = it
            }

            session.driverId?.let {
                this[PreferenceKeys.DRIVER_ID] = it
            }

            session.driverName?.let {
                this[PreferenceKeys.DRIVER_NAME] = it
            }
            session.driverProfile?.let {
                this[PreferenceKeys.DRIVER_PROFILE] = it
            }
            session.vehicleNumber?.let {
                this[PreferenceKeys.VEHICLE_NUMBER] = it
            }

            this[PreferenceKeys.IS_LOGGED_IN] =
                session.isLoggedIn

        }
//        tokenStore.update(
//            session.accessToken,
//            session.refreshToken
//        )

    }

    suspend fun getSession(): UserSession {
        return UserSession(
            accessToken = getAccessToken(),
            driverId = getDriverId(),
            driverName = getDriverName(),
            vehicleNumber = getVehicleNumber(),
            driverProfile = getDriverProfile() ,
            isLoggedIn = isLoggedIn()
        )
    }

    /**
     * Clears only authentication/session related data.
     */
    suspend fun clearSession() {

        preferenceManager.edit {

            remove(PreferenceKeys.ACCESS_TOKEN)

            remove(PreferenceKeys.DRIVER_ID)

            remove(PreferenceKeys.DRIVER_NAME)

            remove(PreferenceKeys.DRIVER_PROFILE)

            remove(PreferenceKeys.VEHICLE_NUMBER)

            remove(PreferenceKeys.IS_LOGGED_IN)
        }
    }



    suspend fun setOnDuty(isOnDuty: Boolean) {
        preferenceManager.saveBoolean(
            PreferenceKeys.IS_ON_DUTY,
            isOnDuty
        )
    }
    suspend fun isOnDuty(): Boolean {
        return preferenceManager
            .getBoolean(PreferenceKeys.IS_ON_DUTY)
            .first()
    }
    fun observeOnDuty(): Flow<Boolean> {
        return preferenceManager.getBoolean(
            PreferenceKeys.IS_ON_DUTY
        )
    }
    suspend fun saveTrackingSession(
        session: TrackingSession
    ) {
        preferenceManager.saveString(
            PreferenceKeys.TRACKING_SESSION,
            gson.toJson(session)
        )
    }

    suspend fun getTrackingSession(): TrackingSession? {

        val json = preferenceManager
            .getString(PreferenceKeys.TRACKING_SESSION)
            .first()
            ?: return null

        return runCatching {
            gson.fromJson(
                json,
                TrackingSession::class.java
            )
        }.getOrNull()
    }

    suspend fun clearTrackingSession() {
        preferenceManager.remove(
            PreferenceKeys.TRACKING_SESSION
        )
    }


    /**
     * Session Expired Manager
     */
    private val _sessionExpired =
        MutableSharedFlow<Unit>(
            replay = 0,
            extraBufferCapacity = 1
        )

    val sessionExpired = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}