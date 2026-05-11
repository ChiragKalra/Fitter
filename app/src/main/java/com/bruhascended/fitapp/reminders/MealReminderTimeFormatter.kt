package com.bruhascended.fitapp.reminders

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar

object MealReminderTimeFormatter {
    fun formatMinutes(context: Context, minutes: Int): String {
        val normalized = MealReminderScheduler.clampMinutes(minutes.toLong())
        val c = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, normalized / 60)
            set(Calendar.MINUTE, normalized % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return DateFormat.getTimeFormat(context).format(c.time)
    }
}
