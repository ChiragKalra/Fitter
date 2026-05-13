package com.bruhascended.fitapp.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.logweight.LogWeightActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class WeightReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != WeightReminderScheduler.ACTION_WEIGHT_ALARM) return
        val followUpAfter = intent.getLongExtra(WeightReminderScheduler.EXTRA_FOLLOW_UP_AFTER_MILLIS, 0L)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (followUpAfter > 0L &&
                    WeightReminderScheduler.hasWeightLoggedAfter(context.applicationContext, followUpAfter)
                ) {
                    WeightReminderScheduler.cancelFollowUp(context.applicationContext)
                    WeightReminderScheduler.rescheduleAll(context.applicationContext)
                    return@launch
                }
                MealReminderChannels.ensureChannel(context)
                showNotification(context)
                val anchor = followUpAfter.takeIf { it > 0L } ?: System.currentTimeMillis()
                WeightReminderScheduler.scheduleFollowUp(context.applicationContext, anchor)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, MealReminderChannels.WEIGHT_REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_monitor_weight)
            .setContentTitle(context.getString(R.string.weight_reminder_notify_title))
            .setContentText(context.getString(R.string.weight_reminder_notify_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openLogWeightIntent(context))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(WEIGHT_REMINDER_NOTIFICATION_ID, notification)
    }

    private fun openLogWeightIntent(context: Context): PendingIntent {
        val launch = Intent(context, LogWeightActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getActivity(context, 77_200, launch, flags)
    }
}

private const val WEIGHT_REMINDER_NOTIFICATION_ID = 77_300
