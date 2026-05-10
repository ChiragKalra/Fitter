package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.NutritionRecord
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
import kotlin.math.roundToInt

object HealthConnectNutritionSync {

    private const val TAG = "HealthConnectNutrition"
    const val DAYS_BACK = 120L

    suspend fun fetchNutritionPairs(client: HealthConnectClient): List<Pair<Food, Entry>> {
        val end = Instant.now()
        val start = end.minus(DAYS_BACK, ChronoUnit.DAYS)
        val request = ReadRecordsRequest(
            recordType = NutritionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = client.readRecords(request)
        return response.records.mapNotNull { record ->
            try {
                mapSingleNutrition(record)
            } catch (e: Exception) {
                Log.w(TAG, "Skip record: ${e.message}")
                null
            }
        }
    }

    fun mapSingleNutrition(record: NutritionRecord): Pair<Food, Entry> {
        val label = record.name?.takeIf { it.isNotBlank() } ?: "Meal"
        val foodName = "$label·${record.startTime.toEpochMilli()}"
        val energyKcal = record.energy?.inKilocalories?.roundToInt() ?: 0
        val nutrients = EnumMap<NutrientType, Double>(NutrientType::class.java)
        record.protein?.inGrams?.let { nutrients[NutrientType.Protein] = it }
        record.totalCarbohydrate?.inGrams?.let { nutrients[NutrientType.Carbs] = it }
        record.totalFat?.inGrams?.let { nutrients[NutrientType.Fat] = it }
        val weightInfo =
            EnumMap<QuantityType, Double>(QuantityType::class.java).apply {
                this[QuantityType.Gram] = 1.0
            }
        val food = Food(
            foodName = foodName,
            calories = energyKcal.toDouble(),
            weightInfo = weightInfo,
            nutrientInfo = nutrients
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
