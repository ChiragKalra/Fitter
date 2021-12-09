package com.bruhascended.db.activity

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.activity.daos.ActivityEntryDao
import com.bruhascended.db.activity.daos.DayEntryDao
import com.bruhascended.db.activity.daos.PeriodicEntryDao
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.activity.entities.PeriodicEntry

import java.util.Date


@Database(
    entities = [
        ActivityEntry::class,
        PeriodicEntry::class,
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
    abstract fun periodicEntryManager(): PeriodicEntryDao
    abstract fun dayEntryManager(): DayEntryDao

    fun getLivePeriodicEntryOf(date: Date): LiveData<PeriodicEntry> {
        return periodicEntryManager().getTimeRangeSumLive(
            date.time,
            date.time + 24 * 60 * 60 * 1000L
        )
    }

    fun getLivePeriodicEntryWeekly(date: Date): LiveData<List<PeriodicEntry>> {
        return periodicEntryManager().getTimeRangeLive(
            date.time - 7 * 24 * 60 * 60 * 1000L,
            date.time + 1 * 24 * 60 * 60 * 1000L
        )
    }

    fun getLivePeriodicEntryOver(startDate: Date, endDate: Date): LiveData<List<PeriodicEntry>> {
        return periodicEntryManager().getTimeRangeLive(
            startDate.time,
            endDate.time
        )
    }

    fun getLiveTotalEntryOver(startDate: Date, endDate: Date): LiveData<PeriodicEntry> {
        return periodicEntryManager().getTimeRangeSumLive(
            startDate.time,
            endDate.time
        )
    }

    fun findPeriodicEntryByStartTime(time: Long) =
        periodicEntryManager().findByStartTime(time)

    fun insertPeriodicEntry(periodicEntry: PeriodicEntry) {
        periodicEntryManager().insert(periodicEntry)
    }

    fun insertPeriodicEntries(periodicEntries: List<PeriodicEntry>) {
        periodicEntryManager().insertAll(periodicEntries)
    }

    fun deletePeriodicEntry(periodicEntry: PeriodicEntry) {
        periodicEntryManager().delete(periodicEntry)
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
