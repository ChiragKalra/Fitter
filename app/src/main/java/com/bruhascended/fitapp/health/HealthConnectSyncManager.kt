package com.bruhascended.fitapp.health

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.repository.WeightEntryRepository
import kotlinx.coroutines.flow.first
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
        syncActivity(client)
        syncNutrition(client)
        syncWeight(client)
    }

    private suspend fun syncActivity(client: HealthConnectClient) {
        val savedToken = preferencesRepository.getPreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY)
        if (savedToken == null) {
            Log.i(TAG, "No activity token found, performing full sync")
            activityRepo.replaceFromHealthConnect(client)
            val newToken = client.getChangesToken(ChangesTokenRequest(setOf(ExerciseSessionRecord::class)))
            preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, newToken)
        } else {
            try {
                var nextToken = savedToken.toString()
                var hasMore = true
                while (hasMore) {
                    val response = client.getChanges(nextToken)
                    for (change in response.changes) {
                        when (change) {
                            is UpsertionChange -> {
                                if (change.record is ExerciseSessionRecord && change.record.metadata.dataOrigin.packageName != context.packageName) {
                                    val entry = HealthConnectActivitySync.mapSingleSession(client, change.record as ExerciseSessionRecord)
                                    if (entry != null) {
                                        activityRepo.upsertFromHcRecord(entry)
                                    }
                                }
                            }
                            is DeletionChange -> {
                                activityRepo.deleteByHcId(change.recordId)
                            }
                        }
                    }
                    nextToken = response.nextChangesToken
                    hasMore = response.hasMore
                }
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, nextToken)
            } catch (e: Exception) {
                Log.w(TAG, "Activity token expired or invalid, performing full sync: ${e.message}")
                activityRepo.replaceFromHealthConnect(client)
                val newToken = client.getChangesToken(ChangesTokenRequest(setOf(ExerciseSessionRecord::class)))
                preferencesRepository.updatePreference(PreferencesKeys.HC_CHANGES_TOKEN_ACTIVITY, newToken)
            }
        }
    }

    private suspend fun syncNutrition(client: HealthConnectClient) {
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
}
