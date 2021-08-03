package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import com.bruhascended.fitapp.R
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*

class DayOfWeekFormatter(
    private val context: Context,
    private val lastDay: Int? = null,
): ValueFormatter() {

    private val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)

    override fun getFormattedValue(value: Float): String {
        val last = lastDay ?: (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)
        return daysOfWeek[(value.toInt() + last + 1) % 7]
    }
}