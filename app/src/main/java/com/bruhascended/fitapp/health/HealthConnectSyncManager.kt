package com.bruhascended.fitapp.health

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ChangesTokenRequest
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.repository.WeightEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthConnectSyncManager(
    private val context: Context,
) {
    private val TAG = "HCSyncManager"
    private val preferencesRepository = PreferencesRepository(context)
    private val activityRepo = ActivityEntryRepository(context)
    private val foodRepo = FoodEntryRepository(context.applicationContext as Application)
    private val weightRepo = WeightEntryRepository(context)

    suspend fun sync(client: HealthConnectClient) = withContext(Dispatchers.IO) {
        logPermissions(client)
        syncActivity(client)
        syncNutrition(client)
        syncWeight(client)
        pushLocalNutritionExports(client)
    }

    private suspend fun logPermissions(client: HealthConnectClient) {
        val granted = client.permissionController.getGrantedPermissions()
        val missingRead = HealthConnectPermissions.readPermissions - granted
        Log.i(
            TAG,
            "HC permissions readGranted=${missingRead.isEmpty()} missingRead=$missingRead " +
                "writeNutritionGranted=${HealthConnectPermissions.writePermissions.all { it in granted }}"
        )
    }

    private suspend fun pushLocalNutritionExports(client: HealthConnectClient) {
        if (!HealthConnectNutritionExport.hasWriteNutrition(client)) return
        val rows = foodRepo.loadAllFoodEntriesSync()
        HealthConnectNutritionExport.syncAllEligibleEntries(client, preferencesRepository, rows)
        Log.i(TAG, "Pushed local nutrition for ${rows.count { it.entry.hcId == null }} HC-eligible row(s)")
    }

    private suspend fun syncActivity(client: HealthConnectClient) {
        ensureActivityImporterSchemaBump()
        val savedToken = preferencesRepository.getPreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY)
        if (savedToken == null) {
            Log.i(TAG, "No activity token found, performing full sync")
            activityRepo.replaceFromHealthConnect(client)
            val newToken = client.getChangesToken(ChangesTokenRequest(ACTIVITY_RECORD_TYPES))
            preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, newToken)
        } else {
            try {
                var nextToken = savedToken.toString()
                var hasMore = true
                while (hasMore) {
                    val response = client.getChanges(nextToken)
                    var needsFullActivityResync = false
                    for (change in response.changes) {
                        when (change) {
                            is UpsertionChange -> {
                                if (change.record.metadata.dataOrigin.packageName != context.packageName) {
                                    when (val record = change.record) {
                                        is ExerciseSessionRecord -> {
                                            val entry = HealthConnectActivitySync.mapSingleSession(client, record)
                                            if (entry != null) {
                                                activityRepo.upsertFromHcRecord(entry)
                                            }
                                        }
                                        is TotalCaloriesBurnedRecord,
                                        is StepsRecord,
                                        is DistanceRecord,
                                        -> needsFullActivityResync = true
                                    }
                                }
                            }
                            is DeletionChange -> needsFullActivityResync = true
                        }
                    }
                    nextToken = response.nextChangesToken
                    hasMore = response.hasMore
                    if (needsFullActivityResync) {
                        Log.i(TAG, "Activity aggregate record changed, performing full activity resync")
                        activityRepo.replaceFromHealthConnect(client)
                        val newToken = client.getChangesToken(ChangesTokenRequest(ACTIVITY_RECORD_TYPES))
                        preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, newToken)
                        return
                    }
                }
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, nextToken)
            } catch (e: Exception) {
                Log.w(TAG, "Activity token expired or invalid, performing full sync: ${e.message}")
                activityRepo.replaceFromHealthConnect(client)
                val newToken = client.getChangesToken(ChangesTokenRequest(ACTIVITY_RECORD_TYPES))
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, newToken)
            }
        }
    }

    private suspend fun syncNutrition(client: HealthConnectClient) {
        ensureNutritionImporterSchemaBump()
        val savedToken = preferencesRepository.getPreference(PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION)
        if (savedToken == null) {
            Log.i(TAG, "No nutrition token found, performing full sync")
            foodRepo.replaceJournalFromHealthConnect(client)
            val newToken = client.getChangesToken(ChangesTokenRequest(setOf(NutritionRecord::class)))
            preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION, newToken)
        } else {
            try {
                var nextToken = savedToken.toString()
                var hasMore = true
                while (hasMore) {
                    val response = client.getChanges(nextToken)
                    for (change in response.changes) {
                        when (change) {
                            is UpsertionChange -> {
                                if (change.record is NutritionRecord && change.record.metadata.dataOrigin.packageName != context.packageName) {
                                    val pair = HealthConnectNutritionSync.mapSingleNutrition(change.record as NutritionRecord)
                                    foodRepo.upsertFromHcRecord(pair.first, pair.second)
                                }
                            }
                            is DeletionChange -> {
                                foodRepo.deleteByHcId(change.recordId)
                            }
                        }
                    }
                    nextToken = response.nextChangesToken
                    hasMore = response.hasMore
                }
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION, nextToken)
            } catch (e: Exception) {
                Log.w(TAG, "Nutrition token expired or invalid, performing full sync: ${e.message}")
                foodRepo.replaceJournalFromHealthConnect(client)
                val newToken = client.getChangesToken(ChangesTokenRequest(setOf(NutritionRecord::class)))
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION, newToken)
            }
        }
    }

    /**
     * When nutrition import semantics change, drop the HC changes token once so history is replaced
     * via full [FoodEntryRepository.replaceJournalFromHealthConnect] on the next sync.
     */
    private fun ensureNutritionImporterSchemaBump() {
        val raw = preferencesRepository.getPreference(PreferencesKeys.HC_NUTRITION_IMPORT_SCHEMA)
        val current = raw as? Int ?: 0
        if (current >= NUTRITION_IMPORT_SCHEMA_CURRENT) return
        Log.i(TAG, "HC nutrition import schema ${current}->$NUTRITION_IMPORT_SCHEMA_CURRENT — full nutrition resync")
        preferencesRepository.removePreference(PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION)
        preferencesRepository.updatePreference(
            PreferencesKeys.HC_NUTRITION_IMPORT_SCHEMA,
            NUTRITION_IMPORT_SCHEMA_CURRENT,
        )
    }

    /**
     * The activity importer stores Health Connect's deduped daily aggregates. Older builds summed
     * raw step/distance/calorie records from every origin, which double-counted overlapping sources.
     */
    private fun ensureActivityImporterSchemaBump() {
        val raw = preferencesRepository.getPreference(PreferencesKeys.HC_ACTIVITY_IMPORT_SCHEMA)
        val current = raw as? Int ?: 0
        if (current >= ACTIVITY_IMPORT_SCHEMA_CURRENT) return
        Log.i(TAG, "HC activity import schema ${current}->$ACTIVITY_IMPORT_SCHEMA_CURRENT - full activity resync")
        preferencesRepository.removePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY)
        preferencesRepository.updatePreference(
            PreferencesKeys.HC_ACTIVITY_IMPORT_SCHEMA,
            ACTIVITY_IMPORT_SCHEMA_CURRENT,
        )
    }

    private suspend fun syncWeight(client: HealthConnectClient) {
        val savedToken = preferencesRepository.getPreference(PreferencesKeys.HC_CHANGES_TOKEN_WEIGHT)
        if (savedToken == null) {
            Log.i(TAG, "No weight token found, performing full sync")
            weightRepo.replaceFromHealthConnect(client)
            val newToken = client.getChangesToken(ChangesTokenRequest(setOf(WeightRecord::class)))
            preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_WEIGHT, newToken)
        } else {
            try {
                var nextToken = savedToken.toString()
                var hasMore = true
                while (hasMore) {
                    val response = client.getChanges(nextToken)
                    for (change in response.changes) {
                        when (change) {
                            is UpsertionChange -> {
                                if (change.record is WeightRecord && change.record.metadata.dataOrigin.packageName != context.packageName) {
                                    val entry = HealthConnectWeightSync.mapSingleWeight(change.record as WeightRecord)
                                    weightRepo.upsertFromHcRecord(entry)
                                }
                            }
                            is DeletionChange -> {
                                weightRepo.deleteByHcId(change.recordId)
                            }
                        }
                    }
                    nextToken = response.nextChangesToken
                    hasMore = response.hasMore
                }
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_WEIGHT, nextToken)
            } catch (e: Exception) {
                Log.w(TAG, "Weight token expired or invalid, performing full sync: ${e.message}")
                weightRepo.replaceFromHealthConnect(client)
                val newToken = client.getChangesToken(ChangesTokenRequest(setOf(WeightRecord::class)))
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_WEIGHT, newToken)
            }
        }
    }

    private companion object {
        private val ACTIVITY_RECORD_TYPES = setOf(
            TotalCaloriesBurnedRecord::class,
            StepsRecord::class,
            DistanceRecord::class,
            ExerciseSessionRecord::class,
        )

        /** Bumped when importer must wipe [PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY]. */
        private const val ACTIVITY_IMPORT_SCHEMA_CURRENT = 4

        /** Bumped when importer must wipe [PreferencesKeys.HC_CHANGES_TOKEN_NUTRITION]. */
        private const val NUTRITION_IMPORT_SCHEMA_CURRENT = 3
    }
}
