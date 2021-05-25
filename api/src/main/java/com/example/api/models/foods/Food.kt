package com.example.api.models.foods

data class Food(
    val additionalDescriptions: String?,
    val allHighlightFields: String,
    val commonNames: String?,
    val dataType: String,
    val description: String,
    val fdcId: Int,
    val brandOwner: String?,
    val foodCategory: String,
    val foodCode: Int?,
    val lowercaseDescription: String,
)