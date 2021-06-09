package com.bruhascended.api.models.foods

import java.io.Serializable

data class Food(
    val description: String,
    val fdcId: Int,
    val foodNutrients: List<Nutrition>,
    val brandOwner: String? = "Non Branded",
) : Serializable