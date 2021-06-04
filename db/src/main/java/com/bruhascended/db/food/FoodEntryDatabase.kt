package com.bruhascended.db.food

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruhascended.db.food.daos.CrossReferenceDao
import com.bruhascended.db.food.daos.EntryDao
import com.bruhascended.db.food.daos.FoodDao
import com.bruhascended.db.food.daos.FoodEntryDao
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.entities.CrossReference


@Database(
    entities = [
        Food::class,
        Entry::class,
        CrossReference::class
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

    abstract fun foodManager(): FoodDao

    abstract fun loadFoodEntry(): FoodEntryDao

    fun insertEntry (foodEntry: FoodEntry) {
        insertEntry (foodEntry.food, foodEntry.entry)
    }

    fun insertEntry (food: Food, entry: Entry) {
        val entryId = entryManager().insert(entry)
        foodManager().insert(food)
        crossRefManager().insert(CrossReference(entryId, food.foodName))
    }

    fun deleteEntry (foodEntry: FoodEntry) {
        crossRefManager().delete(CrossReference(foodEntry))
        entryManager().delete(foodEntry.entry)
    }

    fun deleteEntry (food: Food, entry: Entry) {
        deleteEntry(FoodEntry(entry, food))
    }
}
