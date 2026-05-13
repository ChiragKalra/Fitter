package com.bruhascended.fitapp.repository

import androidx.datastore.preferences.core.*


object PreferencesKeys {
    enum class BoolPreferences(val key: Preferences.Key<Boolean>) {
        SYNC_ENABLED(PreferencesKeys.SYNC_ENABLED),
        REMINDER_LUNCH_ENABLED(PreferencesKeys.REMINDER_LUNCH_ENABLED),
        REMINDER_DINNER_ENABLED(PreferencesKeys.REMINDER_DINNER_ENABLED),
        REMINDER_BREAKFAST_ENABLED(PreferencesKeys.REMINDER_BREAKFAST_ENABLED),
        REMINDER_SNACK_ENABLED(PreferencesKeys.REMINDER_SNACK_ENABLED),
    }

    enum class LongPreferences(val key: Preferences.Key<Long>) {
        GOAL_CALORIE_BURN(PreferencesKeys.GOAL_CALORIE_BURN),
        GOAL_STEPS(PreferencesKeys.GOAL_STEPS),
        GOAL_DURATION(PreferencesKeys.GOAL_DURATION),
        GOAL_DISTANCE(PreferencesKeys.GOAL_DISTANCE),
        GOAL_CALORIE_CONSUMPTION(PreferencesKeys.GOAL_CALORIE_CONSUMPTION),
        GOAL_ADDED_SUGAR(PreferencesKeys.GOAL_ADDED_SUGAR),
        REMINDER_LUNCH_MINUTES(PreferencesKeys.REMINDER_LUNCH_MINUTES),
        REMINDER_DINNER_MINUTES(PreferencesKeys.REMINDER_DINNER_MINUTES),
        REMINDER_BREAKFAST_MINUTES(PreferencesKeys.REMINDER_BREAKFAST_MINUTES),
        REMINDER_SNACK_MINUTES(PreferencesKeys.REMINDER_SNACK_MINUTES),
    }

    val GOAL_CALORIE_NET = intPreferencesKey("GOAL_CALORIE_NET")

    // user Stats
    val SYNC_ENABLED = booleanPreferencesKey("SYNC_ENABLED")

    // Meal logging reminders (minutes since local midnight for each)
    val REMINDER_LUNCH_ENABLED = booleanPreferencesKey("REMINDER_LUNCH_ENABLED")
    val REMINDER_DINNER_ENABLED = booleanPreferencesKey("REMINDER_DINNER_ENABLED")
    val REMINDER_BREAKFAST_ENABLED = booleanPreferencesKey("REMINDER_BREAKFAST_ENABLED")
    val REMINDER_SNACK_ENABLED = booleanPreferencesKey("REMINDER_SNACK_ENABLED")
    val REMINDER_LUNCH_MINUTES = longPreferencesKey("REMINDER_LUNCH_MINUTES")
    val REMINDER_DINNER_MINUTES = longPreferencesKey("REMINDER_DINNER_MINUTES")
    val REMINDER_BREAKFAST_MINUTES = longPreferencesKey("REMINDER_BREAKFAST_MINUTES")
    val REMINDER_SNACK_MINUTES = longPreferencesKey("REMINDER_SNACK_MINUTES")

    // Health Connect Changes API tokens
    val HC_CHANGES_TOKEN_ACTIVITY = stringPreferencesKey("HC_CHANGES_TOKEN_ACTIVITY")
    val HC_CHANGES_TOKEN_NUTRITION = stringPreferencesKey("HC_CHANGES_TOKEN_NUTRITION")
    val HC_CHANGES_TOKEN_WEIGHT = stringPreferencesKey("HC_CHANGES_TOKEN_WEIGHT")

    /**
     * Bumped when Health Connect nutrition import semantics change enough to require wiping the
     * incremental changes token so the next sync re-reads NutritionRecord history.
     */
    val HC_NUTRITION_IMPORT_SCHEMA = intPreferencesKey("HC_NUTRITION_IMPORT_SCHEMA")
    val HC_ACTIVITY_IMPORT_SCHEMA = intPreferencesKey("HC_ACTIVITY_IMPORT_SCHEMA")

    /** One-time UUID so Health Connect nutrition client-record ids remain unique across app reinstalls. */
    val HC_NUTRITION_EXPORT_CLIENT_SALT = stringPreferencesKey("HC_NUTRITION_EXPORT_CLIENT_SALT")

    /** Last successful activity sync start time from Health Connect (epoch ms). */
    val LAST_ACTIVITY_SYNC_TIME = longPreferencesKey("LAST_ACTIVITY_SYNC_TIME")
    /** Last successful periodic sync start time from Health Connect (epoch ms). */
    val LAST_PERIODIC_SYNC_TIME = longPreferencesKey("LAST_PERIODIC_SYNC_TIME")

    // Food Goals
    val GOAL_CALORIE_CONSUMPTION = longPreferencesKey("GOAL_CALORIE_CONSUMPTION")
    val GOAL_PROTEIN = longPreferencesKey("GOAL_PROTEIN")
    val GOAL_CARBS = longPreferencesKey("GOAL_CARBS")
    val GOAL_FAT = longPreferencesKey("GOAL_FAT")
    val GOAL_ADDED_SUGAR = longPreferencesKey("GOAL_ADDED_SUGAR")

    // Activity Goals
    val GOAL_CALORIE_BURN = longPreferencesKey("GOAL_CALORIE_BURN")
    val GOAL_DISTANCE = longPreferencesKey("GOAL_DISTANCE")
    val GOAL_STEPS = longPreferencesKey("GOAL_STEPS")
    val GOAL_DURATION = longPreferencesKey("GOAL_DURATION")

    /** Comma-separated [DashboardSection.persistenceId] lists; see PreferencesRepository.dashboardUiConfigFlow. */
    val DASHBOARD_SECTION_ORDER = stringPreferencesKey("DASHBOARD_SECTION_ORDER")
    val DASHBOARD_SECTION_HIDDEN = stringPreferencesKey("DASHBOARD_SECTION_HIDDEN")
    val DASHBOARD_SECTION_WIDTHS = stringPreferencesKey("DASHBOARD_SECTION_WIDTHS")
    val DASHBOARD_SECTION_HEIGHTS = stringPreferencesKey("DASHBOARD_SECTION_HEIGHTS")
    val DASHBOARD_GRID_SIZE = stringPreferencesKey("DASHBOARD_GRID_SIZE")
}
