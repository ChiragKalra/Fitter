package com.bruhascended.fitapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


class PreferencesRepository(
    private val context: Context
){

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
        var calories: Int,
        var distance: Double,
        var steps: Int,
        var duration: Long,
    )

    object PreferencesKeys {
        val GOAL_CALORIE_NET = intPreferencesKey("GOAL_CALORIE_NET")

        // Food Goals
        val GOAL_CALORIE_CONSUMPTION = intPreferencesKey("GOAL_CALORIE_CONSUMPTION")
        val GOAL_PROTEIN = doublePreferencesKey("GOAL_PROTEIN")
        val GOAL_CARBS = doublePreferencesKey("GOAL_CARBS")
        val GOAL_FAT = doublePreferencesKey("GOAL_FAT")

        // Activity Goals
        val GOAL_CALORIE_BURN = intPreferencesKey("GOAL_CALORIE_BURN")
        val GOAL_DISTANCE = doublePreferencesKey("GOAL_DISTANCE")
        val GOAL_STEPS = intPreferencesKey("GOAL_STEPS")
        val GOAL_DURATION = longPreferencesKey("GOAL_DURATION")
    }


    private val Context.dataStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME
    )

    private val dataStore = context.dataStore

    val activityGoalsFlow: Flow<ActivityPreferences> = dataStore.data
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
                preferences[PreferencesKeys.GOAL_CALORIE_BURN] ?: 200,
                preferences[PreferencesKeys.GOAL_DISTANCE] ?: 1000.0,
                preferences[PreferencesKeys.GOAL_STEPS] ?: 3000,
                preferences[PreferencesKeys.GOAL_DURATION] ?: 60*60*1000L,
            )
        }

    val nutritionGoalsFlow: Flow<NutritionPreferences> = dataStore.data
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
                preferences[PreferencesKeys.GOAL_CALORIE_CONSUMPTION] ?: 1800,
                preferences[PreferencesKeys.GOAL_PROTEIN] ?: 100.0,
                preferences[PreferencesKeys.GOAL_FAT] ?: 100.0,
                preferences[PreferencesKeys.GOAL_CARBS] ?: 100.0,
            )
        }


    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    suspend fun <T> getPreference(key: Preferences.Key<T>) = dataStore.data
        .map {
            it[key]
        }

}