package com.bruhascended.db

import android.content.Context
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.bruhascended.db.food.*
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.QuantityType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*


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
            calories = 50.0,
        ).apply {
            weightInfo[QuantityType.Whole] = 1.0
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
            calories = 100.0,
        ).apply {
            weightInfo[QuantityType.Whole] = 1.0
        }

        val foodEntry = FoodEntry(
            entry = Entry(
                100,
                1.0,
                QuantityType.Whole,
                MealType.Breakfast,
                Calendar.getInstance().timeInMillis
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
