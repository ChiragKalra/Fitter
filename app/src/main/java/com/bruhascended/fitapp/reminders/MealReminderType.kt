package com.bruhascended.fitapp.reminders

import com.bruhascended.fitapp.R

internal enum class MealReminderType(val requestCode: Int) {
    LUNCH(6001),
    DINNER(6002),
    BREAKFAST(6003),
    SNACK(6004),
    ;

    fun titleRes(): Int = when (this) {
        LUNCH -> R.string.meal_reminder_notify_lunch_title
        DINNER -> R.string.meal_reminder_notify_dinner_title
        BREAKFAST -> R.string.meal_reminder_notify_breakfast_title
        SNACK -> R.string.meal_reminder_notify_snack_title
    }

    fun textRes(): Int = when (this) {
        LUNCH -> R.string.meal_reminder_notify_lunch_text
        DINNER -> R.string.meal_reminder_notify_dinner_text
        BREAKFAST -> R.string.meal_reminder_notify_breakfast_text
        SNACK -> R.string.meal_reminder_notify_snack_text
    }

    companion object {
        fun fromOrdinal(ordinal: Int): MealReminderType? =
            values().firstOrNull { it.ordinal == ordinal }
    }
}
