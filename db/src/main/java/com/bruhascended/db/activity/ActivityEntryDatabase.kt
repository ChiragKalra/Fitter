package com.bruhascended.db.activity

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.activity.daos.ActivityEntryDao
import com.bruhascended.db.activity.daos.DayEntryDao
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import java.util.*


@Database(
    entities = [
        ActivityEntry::class,
        DayEntry::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    ActivityEntryConverters::class
)
abstract class ActivityEntryDatabase : RoomDatabase() {
    abstract fun entryManager(): ActivityEntryDao
    abstract fun dayEntryManager(): DayEntryDao

    private fun Int.days() = this * 24 * 60 * 60 * 1000L

    fun getLiveDayRange(startDate: Date, endDate: Date): LiveData<List<DayEntry>> {
        return dayEntryManager().getTimeRangeLive(
            startDate.time,
            endDate.time
        )
    }

    fun getLiveTotal(startDate: Date, endDate: Date): LiveData<DayEntry> {
        return dayEntryManager().getTimeRangeSumLive(
            startDate.time,
            endDate.time
        )
    }

    fun getLiveDayEntryOf(date: Date): LiveData<DayEntry?> {
        return dayEntryManager().getLiveByStartTime(date.time)
    }

    fun getLiveDayEntryWeekly(date: Date): LiveData<List<DayEntry>> {
        val weekStart = Calendar.getInstance().apply {
            time = date
            timeInMillis -= (get(Calendar.DAY_OF_WEEK) - 2).days()
        }.time
        return dayEntryManager().getTimeRangeLive(
            weekStart.time,
            date.time + 1.days()
        )
    }

    fun getLiveDayEntryMonthly(date: Date): LiveData<List<DayEntry>> {
        val monthStart = Calendar.getInstance().apply {
            time = date
            set(Calendar.DAY_OF_MONTH, 1)
        }.time
        return dayEntryManager().getTimeRangeLive(
            monthStart.time,
            date.time + 1.days()
        )
    }

    fun findDayEntryByStartTime(time: Long) =
        dayEntryManager().findByStartTime(time)

    fun insertDayEntry(dayEntry: DayEntry) {
        dayEntryManager().insert(dayEntry)
    }

    fun insertDayEntries(dayEntries: List<DayEntry>) {
        dayEntryManager().insertAll(dayEntries)
    }

    fun deleteDayEntry(dayEntry: DayEntry) {
        dayEntryManager().delete(dayEntry)
    }
}
