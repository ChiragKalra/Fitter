package com.bruhascended.db.food.types

import android.content.Context
import androidx.annotation.StringRes

import com.bruhascended.db.R.string.*

enum class NutrientType (
    @StringRes
    val stringRes: Int
) {
    Energy(energy),
    Carbs(carbs),
    Fat(fat),
    Fiber(fiber),
    Protein(protein);

    fun getString(context: Context) = context.getString(stringRes)
}