package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()
    val liveFoodEntries = foodEntryRepository.loadLiveFoodEntries()

    val lastItemIds = foodEntryRepository.loadLiveLastItem()
    val separatorInfo = foodEntryRepository.loadLiveSeparators()

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}