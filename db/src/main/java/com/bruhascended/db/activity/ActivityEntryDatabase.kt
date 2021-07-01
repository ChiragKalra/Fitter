package com.bruhascended.db.activity

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.activity.daos.ActivityEntryDao
import com.bruhascended.db.activity.entities.ActivityEntry


@Database(
    entities = [
        ActivityEntry::class,
     ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    ActivityEntryConverters::class
)
abstract class ActivityEntryDatabase : RoomDatabase() {
    abstract fun entryManager(): ActivityEntryDao
}
