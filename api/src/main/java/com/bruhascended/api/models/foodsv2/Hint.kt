package com.bruhascended.api.models.foodsv2

import java.io.Serializable

data class Hint(
    val food: Food?,
    val measures: List<Measure>?
): Serializable