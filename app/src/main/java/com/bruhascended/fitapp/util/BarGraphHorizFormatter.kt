package com.bruhascended.fitapp.util

import android.content.Context
import android.util.Log
import com.bruhascended.fitapp.R
import java.util.*
import kotlin.math.abs


fun getWeekList(context: Context): MutableList<BarGraphData> {
    val startTime = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time
    val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)
    val curr = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
    val list = mutableListOf<BarGraphData>()
    for (i in 0..6) {
        val day = BarGraphData(
            x = daysOfWeek[abs((curr - i + 7) % 7)],
            startTime = Date(startTime.time - (i * 86400000))
        )
        list.add(day)
    }
    return list.asReversed()
}