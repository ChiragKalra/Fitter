package com.bruhascended.db.food.types

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.floor

import com.bruhascended.db.R.plurals.*
import com.bruhascended.db.R.string.*


enum class QuantityType (
    @StringRes
    val stringRes: Int,
    @PluralsRes
    val pluralsRes: Int
) {
    Gram(quantity_gram, gram),
    ML(quantity_ml, ml),
    Ounce(quantity_ounce, ounce),
    Serving(quantity_serving, serving),
    Whole(quantity_whole, whole),
    Cup(quantity_cup, cup);

    companion object {
        fun doubleToString(d: Double): String {
            return DecimalFormat(
                "0",
                DecimalFormatSymbols.getInstance(Locale.getDefault())
            ).apply {
                maximumFractionDigits = 2
            }.format(d)
        }
    }

    fun toString(context: Context, quantity: Double) : String {
        return if (quantity == floor(quantity)) {
            context.resources.getQuantityString(pluralsRes, quantity.toInt(), quantity.toInt())
        } else {
            context.getString(stringRes, doubleToString(quantity))
        }
    }
}