package com.ecosphere.partner.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Generic DataStore manager.
 *
 * Responsibility:
 * - Read / Write primitive values.
 * - No business logic.
 */

class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun saveString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getString(key: Preferences.Key<String>): Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[key]
        }

    suspend fun saveBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getBoolean(key: Preferences.Key<Boolean>, defaultValue: Boolean = false
    ): Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }

    suspend fun saveInt(
        key: Preferences.Key<Int>,
        value: Int
    ) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getInt(
        key: Preferences.Key<Int>,
        defaultValue: Int = 0
    ): Flow<Int> =
        dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }

    suspend fun saveLong(
        key: Preferences.Key<Long>,
        value: Long
    ) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getLong(
        key: Preferences.Key<Long>,
        defaultValue: Long = 0L
    ): Flow<Long> =
        dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }

    suspend fun remove(
        key: Preferences.Key<*>
    ) {
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun edit(
        operation: MutablePreferences.() -> Unit
    ) {
        dataStore.edit { preferences ->
            preferences.operation()
        }
    }
}