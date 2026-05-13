package com.bruhascended.fitapp.ui.activityjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date


class ActivityJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val repository by ActivityEntryRepository.Delegate(mApp)

    val lastItems = repository.loadLiveLastItems()

    val activityEntries = repository.loadActivityEntries()
        .map { pagingData ->
            pagingData.map {
                DateSeparatedItem(DateSeparatedItem.ItemType.Item, item = it)
            }
        }
        .map { pagingData ->
            pagingData.insertSeparators { after, before ->
                val afterDate = after?.item?.date
                val beforeDate = before?.item?.date
                if (after == null && before == null) {
                    null
                } else if (before == null) {
                    DateSeparatedItem(DateSeparatedItem.ItemType.Footer)
                } else if (afterDate != null && afterDate <= beforeDate) {
                    null
                } else {
                    DateSeparatedItem(
                        DateSeparatedItem.ItemType.Separator,
                        separator = beforeDate,
                        liveDayEntry = separatorInfoOf(beforeDate!!)
                    )
                }
            }
        }
        .cachedIn(viewModelScope)

    fun separatorInfoOf(date: Date) = repository.loadLiveSeparatorAt(date)

    fun deleteEntry(activityEntry: ActivityEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteEntry(activityEntry)
        }
    }
}
