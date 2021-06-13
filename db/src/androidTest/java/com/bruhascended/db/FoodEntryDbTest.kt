package com.bruhascended.db

import android.content.Context
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.bruhascended.db.food.*
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class FoodEntryDbTest {
    private lateinit var db: FoodEntryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = FoodEntryDatabaseFactory(context).allowMainThreadOperations(false).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun foodDbTest() {
        val food = Food(
            foodName = "Apple",
            healthRating = 4
        ).apply {
            setSingleCalorieInfo(QuantityType.Units, 100.0)
        }

        // insertion test
        db.foodManager().insert(food)
        val byId = db.foodManager().findByName(food.foodName)
        assertEquals(food, byId)

        // deletion test
        db.foodManager().delete(food)
        val afterDelete = db.foodManager().findByName(food.foodName)
        assertEquals(afterDelete, null)
    }

    @Test
    @Throws(Exception::class)
    fun foodEntryDbTest() {
        val food = Food(
            foodName = "Mango",
            healthRating = -2
        ).apply {
            setSingleCalorieInfo(QuantityType.Units, 125.0)
        }
        val foodEntry = FoodEntry(
            entry = Entry (
                food.calorieInfo[QuantityType.Units] ?: 0.0,
                1.0,
                QuantityType.Units,
                0
            ),
            food = food
        )

        // insertion test
        val entryId = db.insertEntry(foodEntry)
        foodEntry.entry.entryId = entryId
        val first = db.loadFoodEntry().singleById(entryId)
        assertEquals(foodEntry, first)

        // deletion test
        db.deleteEntry(foodEntry)
        val afterDelete = db.loadFoodEntry().singleById(entryId)
        assertEquals(null, afterDelete)
    }
}