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
        var calories: Int,
        var proteins: Double,
        var fats: Double,
        var carbs: Double,
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

//    val activityGoalsFlow: ActivityPreferences = runBlocking {
//        dataStore.data
//            .catch { exception ->
//                // dataStore.data throws an IOException when an error is encountered when reading data
//                if (exception is IOException) {
//                    Log.e(TAG, "Error reading preferences.", exception)
//                    emit(emptyPreferences())
//                } else {
//                    throw exception
//                }
//            }.map { preferences ->
//                ActivityPreferences(
//                    preferences[PreferencesKeys.GOAL_CALORIE_BURN] ?: 200,
//                    preferences[PreferencesKeys.GOAL_DISTANCE] ?: 1000.0,
//                    preferences[PreferencesKeys.GOAL_STEPS] ?: 3000,
//                    preferences[PreferencesKeys.GOAL_DURATION] ?: 60 * 60 * 1000L,
//                )
//            }.first()
//    }
//
//
//    val nutritionGoalsFlow: Flow<NutritionPreferences> = dataStore.data
//        .catch { exception ->
//            // dataStore.data throws an IOException when an error is encountered when reading data
//            if (exception is IOException) {
//                Log.e(TAG, "Error reading preferences.", exception)
//                emit(emptyPreferences())
//            } else {
//                throw exception
//            }
//        }.map { preferences ->
//            NutritionPreferences(
//                preferences[PreferencesKeys.GOAL_CALORIE_CONSUMPTION] ?: 1800,
//                preferences[PreferencesKeys.GOAL_PROTEIN] ?: 100.0,
//                preferences[PreferencesKeys.GOAL_FAT] ?: 100.0,
//                preferences[PreferencesKeys.GOAL_CARBS] ?: 100.0,
//            )
//        }


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