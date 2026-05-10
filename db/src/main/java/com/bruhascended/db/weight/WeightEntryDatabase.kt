package com.bruhascended.db.weight

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bruhascended.db.weight.daos.WeightEntryDao
import com.bruhascended.db.weight.entities.WeightEntry


@Database(
    entities = [
        WeightEntry::class,
     ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    WeightEntryConverters::class
)
abstract class WeightEntryDatabase : RoomDatabase() {
    abstract fun entryManager(): WeightEntryDao

    fun clearAllWeightData() {
        runInTransaction {
            entryManager().deleteAll()
        }
    }

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE weight_entry ADD COLUMN hcId TEXT DEFAULT NULL")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Re-align Room identity hash with current entities (no column changes).
            }
        }
    }
}
