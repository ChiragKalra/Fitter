package com.bruhascended.fitapp.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bruhascended.fitapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MealReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != MealReminderScheduler.ACTION_MEAL_ALARM) return
        val ordinal = intent.getIntExtra(MealReminderScheduler.EXTRA_TYPE_ORDINAL, -1)
        val type = MealReminderType.fromOrdinal(ordinal) ?: return
        val followUpAfter = intent.getLongExtra(MealReminderScheduler.EXTRA_FOLLOW_UP_AFTER_MILLIS, 0L)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (followUpAfter > 0L &&
                    MealReminderScheduler.hasFoodLoggedAfter(context.applicationContext, followUpAfter)
                ) {
                    MealReminderScheduler.cancelFollowUp(context.applicationContext, type)
                    MealReminderScheduler.rescheduleAfterTrigger(context.applicationContext)
                    return@launch
                }
                if (followUpAfter == 0L) {
                    MealReminderScheduler.cancelFollowUp(context.applicationContext, type)
                }
                MealReminderChannels.ensureChannel(context)
                showNotification(context, type)
                val anchor = followUpAfter.takeIf { it > 0L } ?: System.currentTimeMillis()
                MealReminderScheduler.scheduleFollowUp(context.applicationContext, type, anchor)
                if (followUpAfter == 0L) {
                    MealReminderScheduler.rescheduleAfterTrigger(context.applicationContext)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, type: MealReminderType) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val openApp = MealReminderLauncher.pendingIntentMainActivity(context)
        val notification = NotificationCompat.Builder(context, MealReminderChannels.MEAL_REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_consumed)
            .setContentTitle(context.getString(type.titleRes()))
            .setContentText(context.getString(type.textRes()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify(MEAL_REMINDER_NOTIFICATION_BASE_ID + type.ordinal, notification)
    }
}

private const val MEAL_REMINDER_NOTIFICATION_BASE_ID = 58_900

internal object MealReminderLauncher {
    fun pendingIntentMainActivity(context: Context): PendingIntent {
        val launch =
            Intent(context, com.bruhascended.fitapp.ui.main.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getActivity(context, 6101, launch, flags)
    }
}
