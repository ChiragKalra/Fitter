package com.bruhascended.api.models.foodsv2

import com.squareup.moshi.Json
import java.io.Serializable

data class Nutrients(
    @Json(name = "ENERC_KCAL") val Energy: Double,
    @Json(name = "CHOCDF") val Carbs: Double,
    @Json(name = "FAT") val Fat: Double,
    @Json(name = "PROCNT") val Protein: Double
) : Serializable {
    val nutrientList = listOf(Carbs, Fat, Protein)
}