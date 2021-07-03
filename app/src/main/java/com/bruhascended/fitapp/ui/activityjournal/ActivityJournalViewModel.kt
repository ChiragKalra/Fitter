package com.bruhascended.fitapp.ui.activityjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.types.ActivityType
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class ActivityJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    data class SeparatorInfo (
        var totalCalories: Int = 0,
        var totalDuration: Long = 0L,
        var totalDistance: Double = .0,
        var totalSteps: Int = 0,
    ) {

        operator fun plusAssign(entry: ActivityEntry) {
            totalCalories += entry.calories
            totalSteps += entry.steps ?: 0
            totalDuration += entry.duration ?: 0
            totalDistance += entry.distance ?: .0
        }

        operator fun plus(entry: ActivityEntry) = SeparatorInfo(
            totalCalories + entry.calories,
            totalDuration + (entry.duration ?: 0),
            totalDistance + (entry.distance ?: .0),
            totalSteps + (entry.steps ?: 0),
        )
    }

    private val repository by ActivityEntryRepository.Delegate(mApp)

    val lastItemLiveSet = MutableLiveData<HashSet<Long>>()
    val separatorInfoMap = MutableLiveData<HashMap<Date, SeparatorInfo>>()

    val activityEntries = repository.loadActivityEntries()
    val liveActivityEntries = repository.loadLiveActivityEntries()

    fun deleteEntry(activityEntry: ActivityEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteEntry(activityEntry)
        }
    }
}
