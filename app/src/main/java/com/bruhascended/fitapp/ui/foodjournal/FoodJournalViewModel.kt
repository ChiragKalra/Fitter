package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()
        .map { pagingData ->
            pagingData.map {
                DateSeparatedItem(
                    DateSeparatedItem.ItemType.Item,
                    item = it
                )
            }
        }
        .map { pagingData ->
            pagingData.insertSeparators { after, before ->
                val afterDate = after?.item?.entry?.date
                val beforeDate = before?.item?.entry?.date
                if (before == null) {
                    DateSeparatedItem(DateSeparatedItem.ItemType.Footer)
                } else if (
                    beforeDate == null || (afterDate != null && afterDate <= beforeDate)
                ) {
                    null
                } else {
                    DateSeparatedItem(
                        DateSeparatedItem.ItemType.Separator,
                        separator = beforeDate,
                        liveDayEntry = getSeparatorInfo(beforeDate)
                    )
                }
            }
        }
        .cachedIn(viewModelScope)

    val liveFoodEntries = foodEntryRepository.loadLiveFoodEntries()

    val lastItemIds = foodEntryRepository.loadLiveLastItem()

    fun getSeparatorInfo(date: Date) =
        foodEntryRepository.loadLiveSeparator(date)

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}