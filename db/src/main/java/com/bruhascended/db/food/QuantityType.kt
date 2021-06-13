package com.bruhascended.db.food

import android.content.Context
import com.bruhascended.db.R

enum class QuantityType (
    private val stringResId: Int
) {
    Grams(R.string.cup),
    Units(R.string.cup),
    MilliLiters(R.string.cup),
    Ounces(R.string.cup),
    Serving(R.string.cup),
    Whole(R.string.cup),
    TableSpoon(R.string.cup),
    Cup(R.string.cup);

    fun toString(context: Context) = context.getString(stringResId)
}