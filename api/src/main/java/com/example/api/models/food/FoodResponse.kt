package com.example.api.models.food

data class FoodResponse(
    val dataType: String,
    val description: String,
    val foodComponents: List<Any>,
    val foodNutrients: List<FoodNutrient>,
    val foodPortions: List<FoodPortion>,
    val inputFoods: List<InputFood>?,
)