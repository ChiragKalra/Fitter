package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.grams
import androidx.health.connect.client.units.kilocalories
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.entities.effectiveDisplayName
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.repository.PreferencesRepository
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Writes locally logged nutrition (excluding entries sourced from HC import via [hcId]) to Health
 * Connect using stable idempotent client record ids — see [nutritionClientRecordId].
 */
object HealthConnectNutritionExport {

    private const val TAG = "HealthConnectNutExport"
    private const val HC_PAGE_SIZE = 500

    suspend fun nutritionClientRecordId(preferencesRepository: PreferencesRepository, entryDbId: Long): String =
        "${preferencesRepository.getOrCreateNutritionExportClientSalt()}_entry_$entryDbId"

    suspend fun hasWriteNutrition(client: HealthConnectClient): Boolean =
        client.permissionController.getGrantedPermissions()
            .contains(HealthPermission.getWritePermission(NutritionRecord::class))

    /**
     * Removes every NutritionRecord authored by [packageName], then callers typically re-import HC.
     * Prevents orphaned Fitter rows after destructive journal replace.
     */
    suspend fun deleteNutritionRecordsAuthoredBy(client: HealthConnectClient, packageName: String) {
        if (!hasWriteNutrition(client)) return
        val origin = DataOrigin(packageName)
        val range =
            TimeRangeFilter.between(Instant.EPOCH, Instant.now().plus(365, ChronoUnit.DAYS))
        val ids = mutableListOf<String>()
        var pageToken: String? = null
        do {
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = range,
                dataOriginFilter = setOf(origin),
                ascendingOrder = true,
                pageSize = HC_PAGE_SIZE,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            response.records.forEach { ids += it.metadata.id }
            pageToken = response.pageToken.takeUnless { it.isNullOrBlank() }
        } while (pageToken != null)

        ids.distinct().chunked(49).forEach { chunk ->
            runCatching {
                client.deleteRecords(NutritionRecord::class, chunk, emptyList())
            }.onFailure { Log.w(TAG, "bulk delete authored chunk (${chunk.size}) ${it.message}") }
        }
        if (ids.isNotEmpty()) {
            Log.i(TAG, "Deleted ${ids.distinct().size} app-origin nutrition record(s) before HC journal replace")
        }
    }

    /**
     * Drops any row previously written for [entryDbId]; safe before re-[insertRecords].
     */
    suspend fun purgeClientNutritionRecords(
        client: HealthConnectClient,
        preferencesRepository: PreferencesRepository,
        entryDbId: Long,
    ) {
        if (!hasWriteNutrition(client)) return
        val key = nutritionClientRecordId(preferencesRepository, entryDbId)
        runCatching {
            client.deleteRecords(NutritionRecord::class, emptyList(), listOf(key))
        }.onFailure { Log.w(TAG, "purge by clientRecordId '$key': ${it.message}") }
    }

    suspend fun upsertExportedFoodEntry(
        client: HealthConnectClient,
        preferencesRepository: PreferencesRepository,
        foodEntry: FoodEntry,
    ) {
        if (!hasWriteNutrition(client)) return
        val entry = foodEntry.entry
        val entryId = entry.entryId ?: return
        // Never duplicate Samsung / HC-imported meals as Fitter-author rows.
        if (entry.hcId != null) return

        purgeClientNutritionRecords(client, preferencesRepository, entryId)
        val record = buildNutritionRecord(foodEntry, preferencesRepository)
        runCatching {
            client.insertRecords(listOf(record))
        }.onFailure { Log.e(TAG, "upsert entry $entryId failed", it) }
    }

    suspend fun syncAllEligibleEntries(
        client: HealthConnectClient,
        preferencesRepository: PreferencesRepository,
        foodEntries: List<FoodEntry>,
    ) {
        if (!hasWriteNutrition(client)) return
        for (fe in foodEntries) {
            val eid = fe.entry.entryId ?: continue
            if (fe.entry.hcId != null) continue
            runCatching {
                purgeClientNutritionRecords(client, preferencesRepository, eid)
                val record = buildNutritionRecord(fe, preferencesRepository)
                client.insertRecords(listOf(record))
            }.onFailure { Log.w(TAG, "sync entry $eid failed: ${it.message}") }
        }
    }

    private suspend fun buildNutritionRecord(
        foodEntry: FoodEntry,
        preferencesRepository: PreferencesRepository,
    ): NutritionRecord {
        val entry = foodEntry.entry
        val food = foodEntry.food
        val entryId = entry.entryId!!

        val start = Instant.ofEpochMilli(entry.timeInMillis).truncatedTo(ChronoUnit.SECONDS)
        val end = start.plus(1, ChronoUnit.MINUTES)
        val offset = ZoneId.systemDefault().rules.getOffset(start)

        val name = food.effectiveDisplayName().take(200)
        val gramsPortion =
            entry.quantity *
                (
                    food.weightInfo[entry.quantityType]
                        ?: when (entry.quantityType) {
                            QuantityType.Gram -> 1.0
                            else -> 1.0
                        }
                    ).coerceAtLeast(0.0)

        fun macroGram(type: NutrientType): androidx.health.connect.client.units.Mass? {
            val perUnit = food.nutrientInfo[type] ?: return null
            val total = perUnit * gramsPortion
            if (total <= 0.0) return null
            return total.grams
        }

        val clientKey = nutritionClientRecordId(preferencesRepository, entryId)
        val metadata = Metadata.manualEntryWithId(clientKey)

        val proteinMass = macroGram(NutrientType.Protein)
        val carbsMass = macroGram(NutrientType.Carbs)
        val fatMass = macroGram(NutrientType.Fat)
        val sugarMass = macroGram(NutrientType.AddedSugar)

        return NutritionRecord(
            biotin = null,
            caffeine = null,
            calcium = null,
            energy = entry.calories.toDouble().kilocalories,
            energyFromFat = null,
            chloride = null,
            cholesterol = null,
            chromium = null,
            copper = null,
            dietaryFiber = null,
            folate = null,
            folicAcid = null,
            iodine = null,
            iron = null,
            magnesium = null,
            manganese = null,
            molybdenum = null,
            monounsaturatedFat = null,
            niacin = null,
            pantothenicAcid = null,
            phosphorus = null,
            polyunsaturatedFat = null,
            potassium = null,
            protein = proteinMass,
            riboflavin = null,
            saturatedFat = null,
            selenium = null,
            sodium = null,
            sugar = sugarMass,
            thiamin = null,
            totalCarbohydrate = carbsMass,
            totalFat = fatMass,
            transFat = null,
            unsaturatedFat = null,
            vitaminA = null,
            vitaminB12 = null,
            vitaminB6 = null,
            vitaminC = null,
            vitaminD = null,
            vitaminE = null,
            vitaminK = null,
            zinc = null,
            name = name,
            mealType = entry.mealType.toHealthConnectMealTypeInt(),
            startTime = start,
            startZoneOffset = offset,
            endTime = end,
            endZoneOffset = offset,
            metadata = metadata,
        )
    }

    private fun MealType.toHealthConnectMealTypeInt(): Int =
        when (this) {
            MealType.Breakfast, MealType.Brunch -> 1
            MealType.Lunch -> 2
            MealType.Dinner -> 3
            MealType.EveningSnack, MealType.LateNightSnack -> 4
            MealType.Other -> 0
        }
}
