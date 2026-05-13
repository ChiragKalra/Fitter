package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.repository.WeightEntryRepository
import com.bruhascended.fitapp.util.BarGraphData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    private val weightEntryRepository by WeightEntryRepository.Delegate(mApp)
    var defaultNutrientData = com.bruhascended.db.food.entities.DayEntry(0L)
    var activityData: LiveData<List<DayEntry>>? = null
    var activityEntries: LiveData<List<ActivityEntry>>? = null
    var nutrientData: LiveData<com.bruhascended.db.food.entities.DayEntry?>? = null
    var foodWeekDayEntries: LiveData<List<com.bruhascended.db.food.entities.DayEntry>>? = null
    var weightEntries: LiveData<List<WeightEntry>>? = null
    var latestFoodEntry: LiveData<Entry?>? = null
    var latestWeightEntry: LiveData<WeightEntry?>? = null

    private val prefsRepository = PreferencesRepository(mApp.applicationContext)

    private val initialDashboardUiConfig = prefsRepository.readDashboardUiConfig()

    val dashboardUiConfig: StateFlow<DashboardUiConfig> =
        prefsRepository.dashboardUiConfigFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialDashboardUiConfig,
        )

    init {
        activityData = getLastWeekDayEntry()
        activityEntries = activityEntryRepository.loadActivityEntriesRangeLive(startDate, endDate)
        nutrientData = getTodayLiveNutrition()
        foodWeekDayEntries = foodEntryRepository.loadFoodDayEntriesRangeLive(startDate, endDate)
        weightEntries = weightEntryRepository.loadEntriesRangeLive(startDate, endDate)
        latestFoodEntry = foodEntryRepository.latestEntryLive()
        latestWeightEntry = weightEntryRepository.latestLive()
    }

    fun saveDashboardLayout(order: List<DashboardSection>, hidden: Set<DashboardSection>) {
        prefsRepository.updateDashboardUiConfig(order, hidden)
    }

    fun saveDashboardCardWidth(section: DashboardSection, widthFraction: Float) {
        prefsRepository.updateDashboardCardWidth(section, widthFraction)
    }

    fun saveDashboardCardShape(
        section: DashboardSection,
        widthFraction: Float,
        heightScale: Float,
    ) {
        prefsRepository.updateDashboardCardShape(section, widthFraction, heightScale)
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

    fun getLastWeekActiveEnergy(
        entries: List<ActivityEntry>,
        weekTemplate: List<BarGraphData>,
    ): MutableList<BarGraphData> {
        val byDay = entries.groupBy { dayStartMillis(Date(it.startTime)) }
        return weekTemplate.mapTo(mutableListOf()) { slot ->
            val key = dayStartMillis(slot.startTime)
            BarGraphData(
                height = byDay[key]?.sumOf { it.calories }?.toFloat() ?: 0f,
                x = slot.x,
                startTime = slot.startTime,
            )
        }
    }

    fun getCalorieBalance(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<DayEntry>,
        weekTemplate: List<BarGraphData>,
    ): MutableList<BarGraphData> {
        val foodByDay = foodDays.associateBy { it.day }
        val burnByDay = activityDays.associateBy { it.startTime }
        return weekTemplate.mapTo(mutableListOf()) { slot ->
            val key = dayStartMillis(slot.startTime)
            val expenditure = burnByDay[key]?.totalCalories ?: 0f
            val consumed = foodByDay[key]?.calories?.toFloat() ?: expenditure
            BarGraphData(consumed - expenditure, slot.x, slot.startTime)
        }
    }

    fun getWeightDelta(
        entries: List<WeightEntry>,
        weekTemplate: List<BarGraphData>,
    ): MutableList<BarGraphData> {
        if (entries.isEmpty()) {
            return weekTemplate.mapTo(mutableListOf()) { BarGraphData(0f, it.x, it.startTime) }
        }
        val sorted = entries.sortedBy { it.timeInMillis }
        val baseline = sorted.first().weightKg()
        return weekTemplate.mapTo(mutableListOf()) { slot ->
            val dayEnd = dayStartMillis(slot.startTime) + ONE_DAY_MILLIS
            val latest = sorted.lastOrNull { it.timeInMillis < dayEnd }?.weightKg() ?: baseline
            BarGraphData((latest - baseline).toFloat(), slot.x, slot.startTime)
        }
    }

    fun getProjectedWeightDelta(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<DayEntry>,
        weekTemplate: List<BarGraphData>,
    ): MutableList<BarGraphData> {
        val balance = getCalorieBalance(foodDays, activityDays, weekTemplate)
        var cumulative = 0f
        return balance.mapTo(mutableListOf()) { point ->
            cumulative += point.height
            BarGraphData(cumulative / KCAL_PER_KG, point.x, point.startTime)
        }
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

    /**
     * Maps each slot in [weekTemplate] (same order/labels as steps/energy charts) to total grams
     * for [type] on that calendar day from food journal aggregates.
     */
    fun getLastWeekNutrientGrams(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        weekTemplate: List<BarGraphData>,
        type: NutrientType,
    ): MutableList<BarGraphData> {
        val byDay = foodDays.associateBy { it.day }
        val out = ArrayList<BarGraphData>(weekTemplate.size)
        for (slot in weekTemplate) {
            val key = dayStartMillis(slot.startTime)
            val grams = byDay[key]?.nutrientInfo?.get(type)?.toFloat() ?: 0f
            out.add(BarGraphData(grams, slot.x, slot.startTime))
        }
        return out
    }

    fun dayStartMillis(d: Date): Long =
        Calendar.getInstance(TimeZone.getDefault()).run {
            time = d
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            timeInMillis
        }

    private fun WeightEntry.weightKg(): Double = weight * type.conversionRatio

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

    companion object {
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val KCAL_PER_KG = 7700f
    }
}
