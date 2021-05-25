package com.example.api.models.foods

import com.squareup.moshi.Json

data class DataType(
    val Branded: Int?,
    @Json(name = "Survey (FNDDS)")val Survey: Int?
)