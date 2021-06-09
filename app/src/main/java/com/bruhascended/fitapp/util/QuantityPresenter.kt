package com.bruhascended.fitapp.util

import android.content.Context
import com.bruhascended.db.food.QuantityType
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.fitapp.R
import kotlin.math.floor

class QuantityPresenter (
    private val mContext: Context,
    private val entry: Entry
) {
    val quantityDescription: String
    get() {
        return if (floor(entry.quantity) == entry.quantity) {
            mContext.resources.getQuantityString(
                when (entry.quantityType) {
                    QuantityType.Units -> R.plurals.Units
                    QuantityType.Grams -> R.plurals.Grams
                    QuantityType.Ounces -> R.plurals.Ounces
                    QuantityType.MilliLiters -> R.plurals.MilliLiters
                },
                floor(entry.quantity).toInt(),
                floor(entry.quantity).toInt(),
            )
        } else {
            entry.quantity.toString() + ' ' + mContext.getString(
                when (entry.quantityType) {
                    QuantityType.Units -> R.string.Units
                    QuantityType.Grams -> R.string.Grams
                    QuantityType.Ounces -> R.string.Ounces
                    QuantityType.MilliLiters -> R.string.MilliLiters
                }
            )
        }
    }
}