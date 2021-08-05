package com.bruhascended.db.activity

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.activity.daos.ActivityEntryDao
import com.bruhascended.db.activity.daos.PeriodicEntryDao
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.PeriodicEntry

import java.util.Date


@Database(
    entities = [
        ActivityEntry::class,
        PeriodicEntry::class
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
}
