package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.util.BarGraphData
import kotlinx.coroutines.launch
import java.util.*


class DashboardViewModel(mApp: Application) : AndroidViewModel(mApp) {


    val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endDate = cal.time
    val startDate = cal.apply {
        add(Calendar.DAY_OF_WEEK, -6)
        set(Calendar.MILLISECOND, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.HOUR_OF_DAY, 0)
    }.time

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)
    private val activityEntryRepository by ActivityEntryRepository.Delegate(mApp)
    var defaultNutrientData = com.bruhascended.db.food.entities.DayEntry(0L)
    var activityData: LiveData<List<DayEntry>>? = null
    var nutrientData: LiveData<com.bruhascended.db.food.entities.DayEntry?>? = null

    init {
        viewModelScope.launch {
            activityData = getLastWeekDayEntry()
            nutrientData = getTodayLiveNutrition()
        }
    }

    fun getLastWeekEnergyExp(
        list: List<DayEntry>,
        energyExpLIst: MutableList<BarGraphData>
    ): MutableList<BarGraphData> {
        energyExpLIst.asReversed()
        val steps = mutableListOf<BarGraphData>()
        for (index in list.indices) {
            val entry =
                BarGraphData(list[index].totalCalories, energyExpLIst[index].x, list[index].date)
            steps.add(entry)
        }
        val addList = dataChecker(steps)
        if (addList != null) {
            for (index in addList.indices) {
                val entry =
                    BarGraphData(
                        addList[index].height,
                        energyExpLIst[steps.size].x,
                        addList[index].startTime
                    )
                steps.add(entry)
            }
        }
        return steps
    }

    fun getLastWeekSteps(
        list: List<DayEntry>,
        stepsLIst: MutableList<BarGraphData>
    ): MutableList<BarGraphData> {
        stepsLIst.asReversed()
        val steps = mutableListOf<BarGraphData>()
        for (index in list.indices) {
            val entry =
                BarGraphData(list[index].totalSteps.toFloat(), stepsLIst[index].x, list[index].date)
            steps.add(entry)
        }
        val addList = dataChecker(steps)
        if (addList != null) {
            for (index in addList.indices) {
                val entry =
                    BarGraphData(
                        addList[index].height,
                        stepsLIst[steps.size].x,
                        addList[index].startTime
                    )
                steps.add(entry)
            }
        }
        return steps
    }

    private fun getTodayLiveNutrition() = foodEntryRepository.loadLiveSeparator(
        Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time
    )

    private fun dataChecker(steps: MutableList<BarGraphData>): MutableList<BarGraphData>? {
        if (steps.size < 7) {
            val diff = 7 - steps.size
            val list = mutableListOf<BarGraphData>()
            for (i in 1..diff) {
                val date = Date(endDate.time - ((i - 1) * 86400000))
                list.add(BarGraphData(startTime = date))
            }
            return list.asReversed()
        } else return null
    }

    private fun getLastWeekDayEntry(): LiveData<List<DayEntry>> {
        return activityEntryRepository.loadRangeDayEntries(startDate, endDate)
    }
}