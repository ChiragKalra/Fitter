package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import com.bruhascended.fitapp.R
import com.github.mikephil.charting.formatter.ValueFormatter

class DayOfWeekFormatter(
    private val context: Context
): ValueFormatter() {

    private val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)

    override fun getFormattedValue(value: Float): String {
        return daysOfWeek[value.toInt() % 7]
    }
}