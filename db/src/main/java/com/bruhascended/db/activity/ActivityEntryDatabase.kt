package com.bruhascended.db.activity

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.activity.daos.ActivityEntryDao
import com.bruhascended.db.activity.daos.DayEntryDao
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry

import java.util.Date


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
        return dayEntryManager().getTimeRangeLive(
            date.time - 7 * 24 * 60 * 60 * 1000L,
            date.time + 1 * 24 * 60 * 60 * 1000L
        )
    }

    fun getLiveDayEntryMonthly(date: Date): LiveData<List<DayEntry>> {
        return dayEntryManager().getTimeRangeLive(
            date.time - 31 * 24 * 60 * 60 * 1000L,
            date.time + 1 * 24 * 60 * 60 * 1000L
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
