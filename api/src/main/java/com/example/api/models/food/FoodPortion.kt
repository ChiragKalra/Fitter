package com.example.api.models.food

data class FoodPortion(
    val gramWeight: Double,
    val id: Int,
    val measureUnit: MeasureUnit,
    val modifier: String,
    val portionDescription: String,
    val sequenceNumber: Int
)