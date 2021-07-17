package com.bruhascended.fitapp.util

import java.util.*

fun Calendar.getTodayMidnightTime(cal: Calendar): Long {
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    return cal.timeInMillis
}

fun Calendar.getStartTime(cal: Calendar): Long {
    cal.timeInMillis = cal.getTodayMidnightTime(cal)
    cal.add(Calendar.WEEK_OF_YEAR, -1)       // TODO SET START TIME HERE
    return timeInMillis
}

