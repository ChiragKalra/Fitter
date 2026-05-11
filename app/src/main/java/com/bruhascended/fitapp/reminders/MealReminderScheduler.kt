package com.bruhascended.fitapp.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.bruhascended.fitapp.repository.PreferencesRepository
import java.util.Calendar
import java.util.TimeZone

object MealReminderScheduler {

    internal const val ACTION_MEAL_ALARM = "com.bruhascended.fitapp.action.MEAL_REMINDER"
    internal const val EXTRA_TYPE_ORDINAL = "extra_meal_reminder_type"

    private const val TAG = "MealReminderScheduler"

    internal fun clampMinutes(raw: Long): Int {
        val m = raw.toInt()
        if (m < 0) return 0
        if (m > 23 * 60 + 59) return 23 * 60 + 59
        return m
    }

    fun rescheduleAll(context: Context) {
        if (!notificationsAllowed(context)) {
            cancelAll(context)
            return
        }
        MealReminderChannels.ensureChannel(context)
        val settings = PreferencesRepository(context).readMealReminderSettings()
        for (type in MealReminderType.values()) {
            if (isEnabled(type, settings)) {
                scheduleOne(context, type, minutesFor(type, settings))
            } else {
                cancelOne(context, type)
            }
        }
    }

    private fun notificationsAllowed(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    internal fun rescheduleAfterTrigger(context: Context) {
        rescheduleAll(context)
    }

    private fun isEnabled(type: MealReminderType, s: PreferencesRepository.MealReminderSettings): Boolean =
        when (type) {
            MealReminderType.LUNCH -> s.lunchEnabled
            MealReminderType.DINNER -> s.dinnerEnabled
            MealReminderType.BREAKFAST -> s.breakfastEnabled
            MealReminderType.SNACK -> s.snackEnabled
        }

    private fun minutesFor(type: MealReminderType, s: PreferencesRepository.MealReminderSettings): Int =
        clampMinutes(
            when (type) {
                MealReminderType.LUNCH -> s.lunchMinutes
                MealReminderType.DINNER -> s.dinnerMinutes
                MealReminderType.BREAKFAST -> s.breakfastMinutes
                MealReminderType.SNACK -> s.snackMinutes
            },
        )

    private fun nextUtcMillisForLocalMinutes(minutesSinceMidnight: Int): Long {
        val zone = TimeZone.getDefault()
        val cal = Calendar.getInstance(zone).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val hour = minutesSinceMidnight / 60
            val minute = minutesSinceMidnight % 60
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return cal.timeInMillis
    }

    private fun alarmPendingIntent(context: Context, type: MealReminderType): PendingIntent {
        val intent = Intent(context, MealReminderReceiver::class.java).apply {
            action = ACTION_MEAL_ALARM
            putExtra(EXTRA_TYPE_ORDINAL, type.ordinal)
        }
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getBroadcast(context, type.requestCode, intent, flags)
    }

    private fun showLauncherPendingIntent(context: Context): PendingIntent {
        val launch = Intent(context, com.bruhascended.fitapp.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        return PendingIntent.getActivity(context, 6100, launch, flags)
    }

    private fun scheduleOne(context: Context, type: MealReminderType, minutesSinceMidnight: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextUtcMillisForLocalMinutes(minutesSinceMidnight)
        val operation = alarmPendingIntent(context, type)
        val showPending = showLauncherPendingIntent(context)
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, showPending)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, operation)
        } catch (e: Exception) {
            Log.e(TAG, "schedule failed for $type", e)
        }
    }

    internal fun cancelOne(context: Context, type: MealReminderType) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent(context, type))
    }

    fun cancelAll(context: Context) {
        for (type in MealReminderType.values()) cancelOne(context, type)
    }
}
