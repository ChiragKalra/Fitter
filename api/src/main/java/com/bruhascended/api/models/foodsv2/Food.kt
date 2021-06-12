package com.bruhascended.api.models.foodsv2

import java.io.Serializable

data class Food(
    val brand: String? = "Non Branded",
    val foodId: String?,
    val image: String?,
    val label: String?,
    val nutrients: Nutrients?
//    val category: String?,
//    val categoryLabel: String?,
//    val servingSizes: List<ServingSize>?,
//    val servingsPerContainer: Double?
): Serializable