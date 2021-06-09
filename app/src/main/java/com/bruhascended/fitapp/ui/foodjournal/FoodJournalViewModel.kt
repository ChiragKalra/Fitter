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

    init {
        CoroutineScope(Dispatchers.IO).launch {
            var id = foodEntryRepository.writeEntry(
                Food("Apple", healthRating = 4),
                Entry(
                    120.0,
                    1.0,
                    QuantityType.Units,
                    Calendar.getInstance().timeInMillis
                )
            )
            id = foodEntryRepository.writeEntry(
                Food("Corn Soup", healthRating = 5),
                Entry(
                    40.0,
                    500.0,
                    QuantityType.MilliLiters,
                    Calendar.getInstance().timeInMillis
                )
            )
            id = foodEntryRepository.writeEntry(
                Food("Fries", healthRating = 1),
                Entry(
                    800.0,
                    200.0,
                    QuantityType.Grams,
                    Calendar.getInstance().timeInMillis
                )
            )
        }
    }
}