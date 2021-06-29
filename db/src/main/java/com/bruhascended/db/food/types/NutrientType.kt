package com.bruhascended.db.food.types

import android.content.Context
import androidx.annotation.StringRes
import com.bruhascended.db.R

import com.bruhascended.db.R.string.*
import kotlin.math.floor

enum class NutrientType (
    @StringRes
    val stringRes: Int
) {
    Carbs(carbs),
    Fat(fat),
    Protein(protein);

    fun getString(context: Context) = context.getString(stringRes)
}