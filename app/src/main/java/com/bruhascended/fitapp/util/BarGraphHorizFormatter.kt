package com.bruhascended.fitapp.util

import android.content.Context
import com.bruhascended.fitapp.R
import java.util.*
import kotlin.math.abs


fun getWeekList(context: Context): MutableList<BarGraphData> {
    val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)
    val curr = Calendar.DAY_OF_WEEK

    var list = mutableListOf<BarGraphData>()
    for(i in 0..6){
        val day =  BarGraphData(x = daysOfWeek[abs((curr-i+7)%7)])
        list.add(day)
    }
    return list.asReversed()
}