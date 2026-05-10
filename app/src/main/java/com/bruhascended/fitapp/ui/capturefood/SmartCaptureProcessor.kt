package com.bruhascended.fitapp.ui.capturefood

import com.bruhascended.db.food.types.QuantityType
import org.json.JSONObject

class SmartCaptureProcessor {
    data class FoodDraft(
        val foodName: String,
        val quantity: Double,
        val quantityType: QuantityType,
        val calories: Double,
        val carbs: Double,
        val fat: Double,
        val protein: Double,
        val addedSugar: Double,
    )

    fun parseGeminiResponse(response: String?): FoodDraft? {
        val jsonText = response
            ?.trim()
            ?.removePrefix("```json")
            ?.removePrefix("```")
            ?.removeSuffix("```")
            ?.trim()
            ?: return null
        val obj = JSONObject(jsonText)
        val foodName = obj.optString("food_name").trim()
        if (foodName.isEmpty()) return null

        return FoodDraft(
            foodName = foodName,
            quantity = obj.optDouble("quantity").takeIf { it > 0.0 } ?: 1.0,
            quantityType = parseQuantityType(obj.optString("quantity_type")),
            calories = optNonNegativeDouble(obj, "calories_kcal"),
            carbs = optNonNegativeDouble(obj, "carbs_g"),
            fat = optNonNegativeDouble(obj, "fat_g"),
            protein = optNonNegativeDouble(obj, "protein_g"),
            addedSugar = optNonNegativeDouble(obj, "added_sugar_g"),
        )
    }

    private fun optNonNegativeDouble(obj: JSONObject, key: String): Double {
        return obj.optDouble(key, 0.0)
            .takeIf { it.isFinite() && it > 0.0 }
            ?: 0.0
    }

    private fun parseQuantityType(value: String): QuantityType {
        return QuantityType.values().firstOrNull {
            it.name.equals(value.trim(), ignoreCase = true)
        } ?: QuantityType.Serving
    }
}
