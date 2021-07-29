package com.bruhascended.fitapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

data class UserPreferences(
    val lastSyncStartTime: Long?,
    val syncEnabled: Boolean
)

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(USER_PREFERENCES_NAME)

class UserPreferenceRepository(val context: Context) {
    object PreferenceKeys {
        val LAST_SYNC_START_TIME = longPreferencesKey("lastSyncStartTime")
        val SYNC_ENABLED = booleanPreferencesKey("syncEnabled")
    }

    val userPreferencesFLow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            Log.d("eyo", "${exception.message}")
        }
        .map { preference ->
            val lastSyncStartTime = preference[PreferenceKeys.LAST_SYNC_START_TIME]
            val syncEnabled = preference[PreferenceKeys.SYNC_ENABLED] ?: true
            UserPreferences(lastSyncStartTime, syncEnabled)
        }

    suspend fun updateLastSyncTime(lastSyncStartTime: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_SYNC_START_TIME] = lastSyncStartTime
        }
    }

    suspend fun updateSyncEnabled(syncEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SYNC_ENABLED] = syncEnabled
        }
    }

    fun<T> getPreference(key: Preferences.Key<T>) = context.dataStore.data
        .map {
            it[key]
        }
}