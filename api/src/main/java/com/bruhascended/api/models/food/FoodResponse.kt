package com.bruhascended.api.models.food

data class FoodResponse(
    val description: String,
    val foodNutrients: List<FoodNutrient>,
)