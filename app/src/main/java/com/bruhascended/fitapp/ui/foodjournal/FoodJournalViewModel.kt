package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.QuantityType
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.*
import java.util.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()
}