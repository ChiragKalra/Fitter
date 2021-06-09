package com.bruhascended.api.models.foods

import java.io.Serializable

data class Nutrition(
    val nutrientId: Int,
    val unitName: String,
    val value: Double
) : Serializable
