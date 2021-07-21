package com.bruhascended.fitapp.ui.activityjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.util.Date


class ActivityJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val repository by ActivityEntryRepository.Delegate(mApp)

    val lastItems = repository.loadLiveLastItems()

    val activityEntries = repository.loadActivityEntries()

    fun separatorInfoOf(date: Date) = repository.loadLiveSeparatorAt(date)

    fun deleteEntry(activityEntry: ActivityEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteEntry(activityEntry)
        }
    }
}
