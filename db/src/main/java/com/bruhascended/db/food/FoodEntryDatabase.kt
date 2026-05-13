package com.bruhascended.db.food

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bruhascended.db.food.daos.*
import com.bruhascended.db.food.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Date


@Database(
    entities = [
        Food::class,
        Entry::class,
        CrossReference::class,
        DayEntry::class
     ],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    FoodEntryConverters::class
)
abstract class FoodEntryDatabase : RoomDatabase() {

    abstract fun entryManager(): EntryDao

    protected abstract fun crossRefManager(): CrossReferenceDao

    protected abstract fun dayEntryManager(): DayEntryDao

    abstract fun foodManager(): FoodDao

    abstract fun loadFoodEntry(): FoodEntryDao

    fun getLiveDayEntry(day: Long): LiveData<DayEntry?> =
        dayEntryManager().getLive(day)

    fun getLiveDayEntriesInRange(startDate: Date, endDate: Date): LiveData<List<DayEntry>> =
        dayEntryManager().getTimeRangeLive(startDate.time, endDate.time)

    fun getFlowDayEntriesInRange(startDate: Date, endDate: Date): Flow<List<DayEntry>> =
        dayEntryManager().getTimeRangeFlow(startDate.time, endDate.time)

    fun getDayEntriesInRangeSync(startDate: Date, endDate: Date): List<DayEntry> =
        dayEntryManager().getTimeRangeSync(startDate.time, endDate.time)

    fun insertEntry (foodEntry: FoodEntry): Long {
        return insertEntry (foodEntry.food, foodEntry.entry)
    }

    fun insertEntry (food: Food, entry: Entry): Long {
        val dayEntry = dayEntryManager().findByDay(entry.date.time) ?:
            DayEntry(entry.date.time)
        dayEntry += FoodEntry(entry, food)
        dayEntryManager().insert(dayEntry)
        val entryId = entryManager().insert(entry)
        foodManager().insert(food)
        crossRefManager().insert(CrossReference(entryId, food.foodName))
        return entryId
    }

    fun deleteEntry (foodEntry: FoodEntry) {
        val dayEntry = dayEntryManager().findByDay(foodEntry.entry.date.time) ?:
            DayEntry(foodEntry.entry.date.time)
        dayEntry -= foodEntry
        dayEntryManager().insert(dayEntry)
        crossRefManager().delete(CrossReference(foodEntry))
        entryManager().delete(foodEntry.entry)
    }

    fun deleteEntry (food: Food, entry: Entry) {
        deleteEntry(FoodEntry(entry, food))
    }

    fun getLiveWeekly(date: Date): LiveData<List<DayEntry>> {
        return dayEntryManager().getTimeRangeLive(
            date.time - 7 * 24 * 60 * 60 * 1000L,
            date.time + 1 * 24 * 60 * 60 * 1000L,
        )
    }

    /**
     * Removes duplicate HC-import rows (same [Entry.hcId]) left by older importer runs.
     * Keeps lowest [Entry.entryId] so foreign links stay deterministic; adjusts [DayEntry] via delete path.
     */
    fun dropDuplicateImportedHealthConnectRows() {
        runInTransaction {
            val HC = entryManager().loadAllSync().filter { !it.hcId.isNullOrBlank() }
            val duplicates = HC.groupBy { it.hcId!! }.filter { (_, rows) -> rows.size > 1 }
            for ((_, rows) in duplicates) {
                val losers = rows.sortedBy { it.entryId ?: Long.MAX_VALUE }.drop(1)
                for (row in losers) {
                    val eid = row.entryId ?: continue
                    val fe = loadFoodEntry().singleById(eid) ?: continue
                    deleteEntry(fe)
                }
            }
        }
    }

    /**
     * Wipes local food journal + aggregates. Used before re-import from Health Connect.
     */
    fun clearAllFoodData() {
        runInTransaction {
            crossRefManager().deleteAll()
            entryManager().deleteAll()
            foodManager().deleteAll()
            dayEntryManager().deleteAll()
        }
    }

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entry ADD COLUMN hcId TEXT DEFAULT NULL")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Re-align Room identity hash with current entities (no column changes).
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Food ADD COLUMN displayTitle TEXT DEFAULT NULL")
            }
        }
    }
}
