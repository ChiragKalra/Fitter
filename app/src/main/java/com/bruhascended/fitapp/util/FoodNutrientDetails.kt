package com.bruhascended.fitapp.util

import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType

data class FoodNutrientDetails(
    var Energy: Double? = null,
    var Carbs: Double? = null,
    var Fat: Double? = null,
    var Protein: Double? = null,
    var quantityType: QuantityType? = null,
    var quantity: Double? = null,
    var mealType: MealType? = null
) {
    fun checkIfNull(): Boolean {
        quantityType ?: return false
        quantity ?: return false
        mealType ?: return false
        return true
    }
}
