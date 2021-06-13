package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.MealType
import com.bruhascended.db.food.QuantityType
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.*
import java.util.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.apply {
                writeEntry(
                    Food("Apple"),
                    Entry(
                        100.0,
                        1.0,
                        QuantityType.Whole,
                        MealType.Breakfast,
                        Calendar.getInstance().timeInMillis
                    )
                )
                writeEntry(
                    Food("Mango"),
                    Entry(
                        200.0,
                        1.0,
                        QuantityType.Whole,
                        MealType.Breakfast,
                        Calendar.getInstance().timeInMillis
                    )
                )
                writeEntry(
                    Food("Rice"),
                    Entry(
                        100.0,
                        1.0,
                        QuantityType.Serving,
                        MealType.Breakfast,
                        Calendar.getInstance().timeInMillis
                    )
                )
            }
        }
    }

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}