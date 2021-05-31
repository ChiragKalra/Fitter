package com.bruhascended.api.models.foods

data class Food(
    val description: String,
    val fdcId: Int,
    val brandOwner: String? = "Non Branded",
)