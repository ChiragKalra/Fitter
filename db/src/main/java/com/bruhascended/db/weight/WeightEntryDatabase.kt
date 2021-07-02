package com.bruhascended.db.weight

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.weight.daos.WeightEntryDao
import com.bruhascended.db.weight.entities.WeightEntry


@Database(
    entities = [
        WeightEntry::class,
     ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    WeightEntryConverters::class
)
abstract class WeightEntryDatabase : RoomDatabase() {
    abstract fun entryManager(): WeightEntryDao
}
