package com.ecosphere.partner.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {

    /**
     * For Authentication
     */
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val DRIVER_ID = stringPreferencesKey("driver_id")
    val DRIVER_NAME = stringPreferencesKey("driver_name")
    val DRIVER_PROFILE = stringPreferencesKey("driver_profile")

    val VEHICLE_ID = stringPreferencesKey("vehicle_id")
    val VEHICLE_NUMBER = stringPreferencesKey("vehicle_number")

    val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")


    /**
     * for Tracking session
     */
    val IS_ON_DUTY = booleanPreferencesKey("is_on_duty")
    val TRACKING_SESSION = stringPreferencesKey("tracking_session")
}