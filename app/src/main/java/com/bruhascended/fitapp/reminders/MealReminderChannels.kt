package com.bruhascended.fitapp.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.bruhascended.fitapp.R

internal object MealReminderChannels {
    const val MEAL_REMINDERS_CHANNEL_ID = "meal_logging_reminders"
    const val WEIGHT_REMINDERS_CHANNEL_ID = "weight_logging_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mealChannel = NotificationChannel(
            MEAL_REMINDERS_CHANNEL_ID,
            context.getString(R.string.meal_reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.meal_reminders_channel_description)
        }
        val weightChannel = NotificationChannel(
            WEIGHT_REMINDERS_CHANNEL_ID,
            context.getString(R.string.weight_reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.weight_reminders_channel_description)
        }
        manager.createNotificationChannels(listOf(mealChannel, weightChannel))
    }
}
