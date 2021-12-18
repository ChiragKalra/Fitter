package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.util.BarGraphData
import java.util.*


class DashboardViewModel(mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)
    private val activityEntryRepository by ActivityEntryRepository.Delegate(mApp)
    val data : LiveData<List<DayEntry>>?

    init {
        data = getLastWeekActivityEntry()
    }

    fun getLastWeekSteps(list: List<DayEntry>, stepsLIst: MutableList<BarGraphData>): MutableList<BarGraphData> {
        val steps = mutableListOf<BarGraphData>()
        list.forEach {
            val entry = BarGraphData(it.totalCalories,stepsLIst[list.indexOf(it)].x)
            steps.add(entry)
        }
        return steps
    }

    fun getLastWeekActivityEntry(): LiveData<List<DayEntry>> {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = cal.time
        cal.add(Calendar.DAY_OF_WEEK, -6)
        val startDate = cal.apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time

        return activityEntryRepository.loadRangeDayEntries(startDate, endDate)
    }
}