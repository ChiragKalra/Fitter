package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}