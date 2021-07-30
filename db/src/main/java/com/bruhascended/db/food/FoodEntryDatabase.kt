package com.bruhascended.db.food

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.food.daos.*
import com.bruhascended.db.food.entities.*


@Database(
    entities = [
        Food::class,
        Entry::class,
        CrossReference::class,
        DayEntry::class
     ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    FoodEntryConverters::class
)
abstract class FoodEntryDatabase : RoomDatabase() {

    protected abstract fun entryManager(): EntryDao

    protected abstract fun crossRefManager(): CrossReferenceDao

    protected abstract fun dayEntryManager(): DayEntryDao

    abstract fun foodManager(): FoodDao

    abstract fun loadFoodEntry(): FoodEntryDao

    fun getLiveDayEntry(day: Long): LiveData<DayEntry?> =
        dayEntryManager().getLive(day)

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
}
