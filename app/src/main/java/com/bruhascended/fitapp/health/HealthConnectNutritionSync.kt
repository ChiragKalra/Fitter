@file:OptIn(androidx.health.connect.client.ExperimentalDeduplicationApi::class)

package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.ExperimentalDeduplicationApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.EnumMap
import java.util.UUID
import kotlin.math.roundToInt

object HealthConnectNutritionSync {

    private const val TAG = "HealthConnectNutrition"
    const val DAYS_BACK = 120L
    private const val PAGE_SIZE = 5000

    suspend fun fetchNutritionPairs(client: HealthConnectClient): List<Pair<Food, Entry>> {
        val end = Instant.now()
        val start = end.minus(DAYS_BACK, ChronoUnit.DAYS)
        val timeRangeFilter = TimeRangeFilter.between(start, end)

        /** Dedupe overlaps/pagination quirks: same HC `metadata.id` must not become two Room rows. */
        val mergedByHcId = linkedMapOf<String, Pair<Food, Entry>>()
        val unstablePairs = mutableListOf<Pair<Food, Entry>>()
        var pageToken: String? = null
        var pageIndex = 0
        do {
            val request = ReadRecordsRequest(
                recordType = NutritionRecord::class,
                timeRangeFilter = timeRangeFilter,
                dataOriginFilter = emptySet<DataOrigin>(),
                ascendingOrder = true,
                pageSize = PAGE_SIZE,
                pageToken = pageToken,
                // Health Connect merges overlapping nutrition rows unless disabled (see HC dedupe strategy 0).
                deduplicateStrategy = 0,
            )
            val response = client.readRecords(request)
            pageIndex++
            if (response.records.isNotEmpty()) {
                Log.d(TAG, "Page $pageIndex: ${response.records.size} raw nutrition record(s) (dedupe off)")
            }
            response.records.forEach { record ->
                try {
                    val pair = mapSingleNutrition(record)
                    val stableId = pair.second.hcId?.takeUnless { it.isBlank() }
                    if (!stableId.isNullOrBlank()) {
                        mergedByHcId[stableId] = pair
                    } else {
                        unstablePairs += pair
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Skip record: ${e.message}")
                }
            }
            pageToken = response.pageToken.takeUnless { it.isNullOrBlank() }
        } while (pageToken != null)

        val out = ArrayList<Pair<Food, Entry>>(mergedByHcId.size + unstablePairs.size)
        out += mergedByHcId.values
        out += unstablePairs
        Log.i(
            TAG,
            "Fetched ${out.size} unique HC meals (${mergedByHcId.size} with stable id) across $pageIndex page(s)",
        )
        return out
    }

    fun mapSingleNutrition(record: NutritionRecord): Pair<Food, Entry> {
        val hcKeySuffix = hcRecordStableSuffix(record.metadata.id)
        val foodNameKey = "hc:$hcKeySuffix"
        val energyKcal = record.energy?.inKilocalories?.roundToInt() ?: 0
        val displayTitle = nutritionDisplayLabelFromHcFields(
            rawName = record.name,
            energyKcal = energyKcal.takeIf { it > 0 },
        )
        val nutrients = EnumMap<NutrientType, Double>(NutrientType::class.java)
        record.protein?.inGrams?.let { nutrients[NutrientType.Protein] = it }
        record.totalCarbohydrate?.inGrams?.let { nutrients[NutrientType.Carbs] = it }
        record.totalFat?.inGrams?.let { nutrients[NutrientType.Fat] = it }
        val weightInfo =
            EnumMap<QuantityType, Double>(QuantityType::class.java).apply {
                this[QuantityType.Gram] = 1.0
            }
        val food = Food(
            foodName = foodNameKey,
            calories = energyKcal.toDouble(),
            weightInfo = weightInfo,
            nutrientInfo = nutrients,
            displayTitle = displayTitle,
        )
        val entry = Entry(
            calories = energyKcal,
            quantity = 1.0,
            quantityType = QuantityType.Gram,
            mealType = record.mapMealType(),
            timeInMillis = record.startTime.toEpochMilli(),
            hcId = record.metadata.id,
        )
        return food to entry
    }

    private fun hcRecordStableSuffix(metadataId: String): String =
        if (metadataId.isNotBlank()) {
            metadataId
        } else {
            "no-id-${UUID.randomUUID()}"
        }

    private fun NutritionRecord.mapMealType(): MealType {
        return when (mealType) {
            1 -> MealType.Breakfast
            2 -> MealType.Lunch
            3 -> MealType.Dinner
            4 -> MealType.EveningSnack
            else -> MealType.getMealTypeAt(startTime.toEpochMilli())
        }
    }
}
