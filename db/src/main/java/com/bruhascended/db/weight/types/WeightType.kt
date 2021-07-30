package com.bruhascended.db.weight.types

import android.content.Context
import androidx.annotation.StringRes

import com.bruhascended.db.R.string.*

enum class WeightType (
    @StringRes
    val stringRes: Int,
    val conversionRatio: Double
) {
    Kilogram(quantity_kg, 1.0),
    Pound(quantity_pound, 0.45359237);

    fun getString(context: Context) = context.getString(stringRes)
}