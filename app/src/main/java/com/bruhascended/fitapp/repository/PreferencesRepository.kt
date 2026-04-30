package com.bruhascended.fitapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

private val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class PreferencesRepository(
    private val context: Context
) {

    companion object {
        private const val USER_PREFERENCES_NAME = "user_preferences"
        const val TAG = "UserPreferencesRepo"
    }

    data class NutritionPreferences(
        var calories: Long,
        var proteins: Long,
        var fats: Long,
        var carbs: Long,
    )

    data class ActivityPreferences(
        var calories: Long,
        var distance: Long,
        var steps: Long,
        var duration: Long,
    )

    data class UserStats(
        var syncEnabled: Boolean
    )


    private val dataStore = context.dataStore

    val userStatsFlow: Flow<UserStats> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserStats(
                preferences[PreferencesKeys.SYNC_ENABLED] ?: false
            )
        }

    val activityGoalsFlow: ActivityPreferences = runBlocking {
        dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences.", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                ActivityPreferences(
                    preferences[PreferencesKeys.GOAL_CALORIE_BURN] ?: 3000L,
                    preferences[PreferencesKeys.GOAL_DISTANCE] ?: 1000L,
                    preferences[PreferencesKeys.GOAL_STEPS] ?: 5000L,
                    preferences[PreferencesKeys.GOAL_DURATION] ?: 60L * 60L * 1000L,
                )
            }.first()
    }

    val nutritionGoalsFlow: NutritionPreferences = runBlocking {
        dataStore.data
            .catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    Log.e(TAG, "Error reading preferences.", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                NutritionPreferences(
                    preferences[PreferencesKeys.GOAL_CALORIE_CONSUMPTION] ?: 2600L,
                    preferences[PreferencesKeys.GOAL_PROTEIN] ?: 60L,
                    preferences[PreferencesKeys.GOAL_FAT] ?: 60L,
                    preferences[PreferencesKeys.GOAL_CARBS] ?: 250L,
                )
            }.first()
    }


    fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun <T> getPreference(key: Preferences.Key<T>) = runBlocking {
        dataStore.data.first().asMap()[key]
    }

}