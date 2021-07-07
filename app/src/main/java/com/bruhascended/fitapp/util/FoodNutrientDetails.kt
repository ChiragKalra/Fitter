package com.bruhascended.fitapp.util

import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import java.io.Serializable

data class FoodNutrientDetails(
    var Energy: Double? = null,
    var Carbs: Double? = null,
    var Fat: Double? = null,
    var Protein: Double? = null,
    var quantityType: QuantityType? = null,
    var quantity: Double? = null,
    var mealType: MealType? = null
): Serializable {
    fun checkIfNull(): Boolean {
        Energy ?: return false
        quantityType ?: return false
        quantity ?: return false
        mealType ?: return false
        return true
    }

    fun checkIfNutrientsNull(): Boolean {
        Carbs ?: return false
        Fat ?: return false
        Protein ?: return false
        return true
    }

    fun getNutrientList(): MutableList<Double?> = mutableListOf(Carbs, Fat, Protein)

}