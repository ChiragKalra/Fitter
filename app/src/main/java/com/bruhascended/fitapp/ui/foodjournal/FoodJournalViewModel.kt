package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    data class SeparatorInfo (
        var totalCalories: Int = 0,
        val totalNutrients: EnumMap<NutrientType, Double> =
            EnumMap<NutrientType, Double>(NutrientType::class.java)
    ) {
        operator fun plus(other: SeparatorInfo) = SeparatorInfo(
            totalCalories + other.totalCalories,
        ).also {
            totalNutrients.forEach { (k, v) ->
                it.totalNutrients[k] = v + (it.totalNutrients[k] ?: .0)
            }
            other.totalNutrients.forEach { (k, v) ->
                it.totalNutrients[k] = v + (it.totalNutrients[k] ?: .0)
            }
        }
    }

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val lastItemLiveSet = MutableLiveData<HashSet<Long>>()
    val separatorInfoMap = MutableLiveData<HashMap<Date, SeparatorInfo>>()

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()
    val liveFoodEntries = foodEntryRepository.loadLiveFoodEntries()

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}