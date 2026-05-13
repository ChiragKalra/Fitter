package com.bruhascended.fitapp.reminders

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.bruhascended.fitapp.repository.WeightEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WeightReminderScheduler {

    internal const val ACTION_WEIGHT_ALARM = "com.bruhascended.fitapp.action.WEIGHT_REMINDER"
    internal const val EXTRA_FOLLOW_UP_AFTER_MILLIS = "extra_weight_follow_up_after_millis"

    private const val TAG = "WeightReminderScheduler"
    private const val FIRST_REMINDER_DELAY_MILLIS = 3 * 24 * 60 * 60 * 1000L
    private const val FOLLOW_UP_DELAY_MILLIS = 12 * 60 * 60 * 1000L
    private const val REQUEST_CODE_MAIN = 77_100
    private const val REQUEST_CODE_FOLLOW_UP = 77_101
    private const val MIN_OVERDUE_DELAY_MILLIS = 60 * 1000L

    fun rescheduleAll(context: Context) {
        val appContext = context.applicationContext
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled; cancelling weight reminders")
            cancelAll(appContext)
            return
        }
        MealReminderChannels.ensureChannel(appContext)
        CoroutineScope(Dispatchers.IO).launch {
            val app = appContext as? Application ?: return@launch
            val lastLoggedAt = WeightEntryRepository(app).latestSync()?.timeInMillis
            val now = System.currentTimeMillis()
            val anchor = lastLoggedAt ?: now
            val triggerAt = maxOf(anchor + FIRST_REMINDER_DELAY_MILLIS, now + MIN_OVERDUE_DELAY_MILLIS)
            schedule(appContext, triggerAt, REQUEST_CODE_MAIN, anchor)
            cancelFollowUp(appContext)
            Log.i(TAG, "Scheduled weight reminder triggerAt=$triggerAt anchor=$anchor")
        }
    }

    internal fun hasWeightLoggedAfter(context: Context, timeInMillis: Long): Boolean {
        val app = context.applicationContext as? Application ?: return false
        val latest = WeightEntryRepository(app).latestSync()?.timeInMillis ?: return false
        return latest > timeInMillis
    }

    internal fun scheduleFollowUp(context: Context, followUpAfterMillis: Long) {
        val triggerAt = System.currentTimeMillis() + FOLLOW_UP_DELAY_MILLIS
        schedule(context.applicationContext, triggerAt, REQUEST_CODE_FOLLOW_UP, followUpAfterMillis)
        Log.i(TAG, "Scheduled weight follow-up triggerAt=$triggerAt anchor=$followUpAfterMillis")
    }

    internal fun cancelFollowUp(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, REQUEST_CODE_FOLLOW_UP))
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, REQUEST_CODE_MAIN))
        alarmManager.cancel(pendingIntent(context, REQUEST_CODE_FOLLOW_UP))
    }

    private fun schedule(context: Context, triggerAt: Long, requestCode: Int, followUpAfterMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val operation = pendingIntent(context, requestCode, followUpAfterMillis)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, operation)
        }
    }

    private fun pendingIntent(
        context: Context,
        requestCode: Int,
        followUpAfterMillis: Long = 0L,
    ): PendingIntent {
        val intent = Intent(context, WeightReminderReceiver::class.java).apply {
            action = ACTION_WEIGHT_ALARM
            if (followUpAfterMillis > 0L) {
                putExtra(EXTRA_FOLLOW_UP_AFTER_MILLIS, followUpAfterMillis)
            }
        }
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }
}
