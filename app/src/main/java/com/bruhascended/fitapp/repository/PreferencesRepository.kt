package com.bruhascended.fitapp.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bruhascended.fitapp.ui.dashboard.DashboardSection
import com.bruhascended.fitapp.ui.dashboard.DashboardGridSize
import com.bruhascended.fitapp.ui.dashboard.DashboardUiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.UUID

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
        var addedSugar: Long,
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

    data class MealReminderSettings(
        val lunchEnabled: Boolean,
        val dinnerEnabled: Boolean,
        val breakfastEnabled: Boolean,
        val snackEnabled: Boolean,
        val lunchMinutes: Long,
        val dinnerMinutes: Long,
        val breakfastMinutes: Long,
        val snackMinutes: Long,
    )

    object DefaultMealReminders {
        const val LUNCH_MINUTES = 15 * 60 // 15:00
        const val DINNER_MINUTES = 22 * 60 // 22:00
        const val BREAKFAST_MINUTES = 8 * 60 // 08:00
        const val SNACK_MINUTES = 19 * 60 // 19:00
    }

    private val dataStore = context.dataStore

    val dashboardUiConfigFlow: Flow<DashboardUiConfig> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading dashboard UI preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences.toDashboardUiConfig() }

    fun readDashboardUiConfig(): DashboardUiConfig {
        return runBlocking {
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        Log.e(TAG, "Error reading dashboard UI preferences.", exception)
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .first()
                .toDashboardUiConfig()
        }
    }

    private fun Preferences.toDashboardUiConfig(): DashboardUiConfig {
        return DashboardUiConfig(
            order = DashboardSection.parseOrder(this[PreferencesKeys.DASHBOARD_SECTION_ORDER]),
            hiddenIds = DashboardSection.parseHidden(this[PreferencesKeys.DASHBOARD_SECTION_HIDDEN]),
            widthFractions =
                DashboardSection.parseWidthFractions(this[PreferencesKeys.DASHBOARD_SECTION_WIDTHS]),
            heightScales =
                DashboardSection.parseHeightScales(this[PreferencesKeys.DASHBOARD_SECTION_HEIGHTS]),
            gridSize = DashboardGridSize.parse(this[PreferencesKeys.DASHBOARD_GRID_SIZE]),
        )
    }

    fun updateDashboardUiConfig(order: List<DashboardSection>, hiddenIds: Set<DashboardSection>) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.DASHBOARD_SECTION_ORDER] =
                    order.joinToString(",") { it.persistenceId }
                prefs[PreferencesKeys.DASHBOARD_SECTION_HIDDEN] =
                    hiddenIds.joinToString(",") { it.persistenceId }
            }
        }
    }

    fun updateDashboardCardWidth(section: DashboardSection, widthFraction: Float) {
        runBlocking {
            dataStore.edit { prefs ->
                val widths =
                    DashboardSection.parseWidthFractions(
                        prefs[PreferencesKeys.DASHBOARD_SECTION_WIDTHS],
                    ).toMutableMap()
                val clampedWidth = DashboardUiConfig.clampWidthFraction(widthFraction)
                if (clampedWidth >= DashboardUiConfig.MAX_CARD_WIDTH_FRACTION) {
                    widths.remove(section)
                } else {
                    widths[section] = clampedWidth
                }
                val serialized = DashboardSection.serializeWidthFractions(widths)
                if (serialized.isBlank()) {
                    prefs.remove(PreferencesKeys.DASHBOARD_SECTION_WIDTHS)
                } else {
                    prefs[PreferencesKeys.DASHBOARD_SECTION_WIDTHS] = serialized
                }
            }
        }
    }

    fun updateDashboardCardShape(
        section: DashboardSection,
        widthFraction: Float,
        heightScale: Float,
    ) {
        runBlocking {
            dataStore.edit { prefs ->
                val widths =
                    DashboardSection.parseWidthFractions(
                        prefs[PreferencesKeys.DASHBOARD_SECTION_WIDTHS],
                    ).toMutableMap()
                val heights =
                    DashboardSection.parseHeightScales(
                        prefs[PreferencesKeys.DASHBOARD_SECTION_HEIGHTS],
                    ).toMutableMap()

                val clampedWidth = DashboardUiConfig.clampWidthFraction(widthFraction)
                val clampedHeight = DashboardUiConfig.clampHeightScale(heightScale)

                if (clampedWidth >= DashboardUiConfig.MAX_CARD_WIDTH_FRACTION) {
                    widths.remove(section)
                } else {
                    widths[section] = clampedWidth
                }

                if (clampedHeight == DashboardUiConfig.DEFAULT_CARD_HEIGHT_SCALE) {
                    heights.remove(section)
                } else {
                    heights[section] = clampedHeight
                }

                val serializedWidths = DashboardSection.serializeWidthFractions(widths)
                if (serializedWidths.isBlank()) {
                    prefs.remove(PreferencesKeys.DASHBOARD_SECTION_WIDTHS)
                } else {
                    prefs[PreferencesKeys.DASHBOARD_SECTION_WIDTHS] = serializedWidths
                }

                val serializedHeights = DashboardSection.serializeHeightScales(heights)
                if (serializedHeights.isBlank()) {
                    prefs.remove(PreferencesKeys.DASHBOARD_SECTION_HEIGHTS)
                } else {
                    prefs[PreferencesKeys.DASHBOARD_SECTION_HEIGHTS] = serializedHeights
                }
            }
        }
    }

    fun updateDashboardGridSize(gridSize: DashboardGridSize) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[PreferencesKeys.DASHBOARD_GRID_SIZE] = gridSize.persistenceId
            }
        }
    }

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
                    preferences[PreferencesKeys.GOAL_ADDED_SUGAR] ?: 50L,
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

    fun <T> removePreference(key: Preferences.Key<T>) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs.remove(key)
            }
        }
    }

    fun <T> getPreference(key: Preferences.Key<T>) = runBlocking {
        dataStore.data.first().asMap()[key]
    }

    /**
     * Persistent salt for Health Connect [androidx.health.connect.client.records.metadata.Metadata] client-record ids so
     * reinstalling Fitter doesn't reuse low `entryId` values and collide with old writes.
     */
    suspend fun getOrCreateNutritionExportClientSalt(): String {
        val prefs = dataStore.data.first()
        val existing = prefs[PreferencesKeys.HC_NUTRITION_EXPORT_CLIENT_SALT]?.takeIf { it.isNotBlank() }
        if (existing != null) return existing
        val generated = UUID.randomUUID().toString()
        dataStore.edit { it[PreferencesKeys.HC_NUTRITION_EXPORT_CLIENT_SALT] = generated }
        return generated
    }

    fun readMealReminderSettings(): MealReminderSettings {
        val p = runBlocking {
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        Log.e(TAG, "Error reading meal reminder preferences.", exception)
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .first()
        }

        fun longOrDefault(raw: Preferences.Key<Long>, minutesDefault: Long): Long =
            p[raw] ?: minutesDefault

        fun booleanOrDefault(raw: Preferences.Key<Boolean>, missingDefault: Boolean): Boolean =
            p[raw] ?: missingDefault

        return MealReminderSettings(
            lunchEnabled = booleanOrDefault(PreferencesKeys.REMINDER_LUNCH_ENABLED, true),
            dinnerEnabled = booleanOrDefault(PreferencesKeys.REMINDER_DINNER_ENABLED, true),
            breakfastEnabled = booleanOrDefault(PreferencesKeys.REMINDER_BREAKFAST_ENABLED, false),
            snackEnabled = booleanOrDefault(PreferencesKeys.REMINDER_SNACK_ENABLED, false),
            lunchMinutes = longOrDefault(
                PreferencesKeys.REMINDER_LUNCH_MINUTES,
                DefaultMealReminders.LUNCH_MINUTES.toLong(),
            ),
            dinnerMinutes = longOrDefault(
                PreferencesKeys.REMINDER_DINNER_MINUTES,
                DefaultMealReminders.DINNER_MINUTES.toLong(),
            ),
            breakfastMinutes = longOrDefault(
                PreferencesKeys.REMINDER_BREAKFAST_MINUTES,
                DefaultMealReminders.BREAKFAST_MINUTES.toLong(),
            ),
            snackMinutes = longOrDefault(
                PreferencesKeys.REMINDER_SNACK_MINUTES,
                DefaultMealReminders.SNACK_MINUTES.toLong(),
            ),
        )
    }

}
