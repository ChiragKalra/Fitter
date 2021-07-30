package com.bruhascended.fitapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

data class UserPreferences(
    val isSignedIn: Boolean,
    val signInStatus: String?,
    val lastPeriodicSyncStartTime: Long?,
    val lastActivitySyncStartTime: Long?,
    val syncEnabled: Boolean
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(USER_PREFERENCES_NAME)

class UserPreferenceRepository(val context: Context) {
    object PreferenceKeys {
        val SIGN_IN_STATUS = stringPreferencesKey("signInStatus")
        val IS_SIGNED_IN = booleanPreferencesKey("isSignedIn")
        val LAST_PERIODIC_SYNC_START_TIME = longPreferencesKey("lastPeriodicSyncStartTime")
        val LAST_ACTIVITY_SYNC_START_TIME = longPreferencesKey("lastActivitySyncStartTime")
        val SYNC_ENABLED = booleanPreferencesKey("syncEnabled")
    }

    val userPreferencesFLow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            Log.d("eyo", "${exception.message}")
        }
        .map { preference ->
            val lastSyncStartTime = preference[PreferenceKeys.LAST_PERIODIC_SYNC_START_TIME]
            val syncEnabled = preference[PreferenceKeys.SYNC_ENABLED] ?: true
            val lastActivitySyncStartTime = preference[PreferenceKeys.LAST_ACTIVITY_SYNC_START_TIME]
            val signInStatus = preference[PreferenceKeys.SIGN_IN_STATUS] ?: "Sign In"
            val isSignedIn = preference[PreferenceKeys.IS_SIGNED_IN] ?: false
            UserPreferences(
                isSignedIn,
                signInStatus,
                lastSyncStartTime,
                lastActivitySyncStartTime,
                syncEnabled
            )
        }

    suspend fun updateLastSyncTime(lastSyncStartTime: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_PERIODIC_SYNC_START_TIME] = lastSyncStartTime
        }
    }

    suspend fun updateLastActivitySyncTime(lastSyncStartTime: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_ACTIVITY_SYNC_START_TIME] = lastSyncStartTime
        }
    }

    suspend fun updateSyncEnabled(syncEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SYNC_ENABLED] = syncEnabled
        }
    }

    suspend fun updateSignInStatus(email: String?) {
        context.dataStore.edit { preference ->
            preference[PreferenceKeys.SIGN_IN_STATUS] = email.toString()
        }
    }

    fun <T> getPreference(key: Preferences.Key<T>) = context.dataStore.data
        .map {
            it[key]
        }
}