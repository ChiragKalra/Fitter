package com.bruhascended.fitapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

internal class MealReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) return
        MealReminderScheduler.rescheduleAll(context.applicationContext)
        WeightReminderScheduler.rescheduleAll(context.applicationContext)
    }
}
