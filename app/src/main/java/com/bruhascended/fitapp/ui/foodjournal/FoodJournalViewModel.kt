package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()


    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}