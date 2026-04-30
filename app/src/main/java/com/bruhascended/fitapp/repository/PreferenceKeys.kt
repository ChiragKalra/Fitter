package com.bruhascended.fitapp.repository

import androidx.datastore.preferences.core.*


object PreferencesKeys {
    enum class BoolPreferences(val key: Preferences.Key<Boolean>) {
        SYNC_ENABLED(PreferencesKeys.SYNC_ENABLED)
    }

    enum class LongPreferences(val key: Preferences.Key<Long>) {
        GOAL_CALORIE_BURN(PreferencesKeys.GOAL_CALORIE_BURN),
        GOAL_STEPS(PreferencesKeys.GOAL_STEPS),
        GOAL_DURATION(PreferencesKeys.GOAL_DURATION),
        GOAL_DISTANCE(PreferencesKeys.GOAL_DISTANCE),
        GOAL_CALORIE_CONSUMPTION(PreferencesKeys.GOAL_CALORIE_CONSUMPTION)
    }

    val GOAL_CALORIE_NET = intPreferencesKey("GOAL_CALORIE_NET")

    // user Stats
    val SYNC_ENABLED = booleanPreferencesKey("SYNC_ENABLED")

    // sync
    val LAST_PERIODIC_SYNC_TIME = longPreferencesKey("LAST_PERIODIC_SYNC_TIME")
    val LAST_ACTIVITY_SYNC_TIME = longPreferencesKey("LAST_ACTIVITY_SYNC_TIME")

    // Food Goals
    val GOAL_CALORIE_CONSUMPTION = longPreferencesKey("GOAL_CALORIE_CONSUMPTION")
    val GOAL_PROTEIN = longPreferencesKey("GOAL_PROTEIN")
    val GOAL_CARBS = longPreferencesKey("GOAL_CARBS")
    val GOAL_FAT = longPreferencesKey("GOAL_FAT")

    // Activity Goals
    val GOAL_CALORIE_BURN = longPreferencesKey("GOAL_CALORIE_BURN")
    val GOAL_DISTANCE = longPreferencesKey("GOAL_DISTANCE")
    val GOAL_STEPS = longPreferencesKey("GOAL_STEPS")
    val GOAL_DURATION = longPreferencesKey("GOAL_DURATION")
}