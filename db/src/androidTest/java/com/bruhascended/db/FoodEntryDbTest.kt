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
        db = FoodEntryDatabaseFactory(context).with(allowMainThread = false)
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
            setCalorieInfo(QuantityType.Count, 100f)
        }

        db.foodManager().insert(food)
        val byId = db.foodManager().findByName(food.foodName)

        assertEquals(food, byId)
    }

    @Test
    @Throws(Exception::class)
    fun foodEntryDbTest() {
        val food = Food(
            foodName = "Mango",
            healthRating = -2
        ).apply {
            setCalorieInfo(QuantityType.Count, 125f)
        }
        val foodEntry = FoodEntry(
            entry = Entry (
                1f,
                QuantityType.Count,
                0
            ),
            food = food
        )

        val entryId = db.insertEntry(foodEntry)
        val first = db.loadFoodEntry().singleById(entryId)

        assertEquals(foodEntry, first)
    }
}