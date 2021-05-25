package com.example.api.models.food

data class InputFood(
    val amount: Double,
    val foodDescription: String,
    val id: Int,
    val ingredientCode: Int,
    val ingredientDescription: String,
    val ingredientWeight: Double,
    val portionCode: String,
    val portionDescription: String,
    val sequenceNumber: Int,
    val unit: String
)