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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
        // Initial setup for LiveDatas (legacy, still used for today's summary)
        activityData = activityEntryRepository.loadRangeDayEntries(startDate, endDate)
        nutrientData = foodEntryRepository.loadLiveSeparator(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
        )
        foodWeekDayEntries = foodEntryRepository.loadFoodDayEntriesRangeLive(startDate, endDate)
        weightEntries = weightEntryRepository.loadEntriesRangeLive(startDate, endDate)
        latestFoodEntry = foodEntryRepository.latestEntryLive()
        latestWeightEntry = weightEntryRepository.latestLive()
        activityEntries = activityEntryRepository.loadActivityEntriesRangeLive(startDate, endDate)
    }

    // New Flow-based pipeline for stable, pre-computed UI data
    val stepsListData = activityEntryRepository.loadRangeDayEntriesFlow(startDate, endDate)
        .map { list -> computeStepsList(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val energyExpListData = activityEntryRepository.loadRangeDayEntriesFlow(startDate, endDate)
        .map { list -> computeEnergyExpList(list) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeEnergyListData = activityEntryRepository.loadActivityEntriesRangeFlow(startDate, endDate)
        .map { entries -> computeActiveEnergyList(entries) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val calorieBalanceListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .combine(activityEntryRepository.loadRangeDayEntriesFlow(startDate, endDate)) { foodDays, activityDays ->
            computeCalorieBalance(foodDays, activityDays)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weightDeltaListData = weightEntryRepository.loadEntriesRangeFlow(startDate, endDate)
        .map { entries -> computeWeightDelta(entries) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projectedWeightListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .combine(activityEntryRepository.loadRangeDayEntriesFlow(startDate, endDate)) { foodDays, activityDays ->
            computeProjectedWeightDelta(foodDays, activityDays)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val proteinListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .map { foodDays -> computeNutrientGrams(foodDays, NutrientType.Protein) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val carbsListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .map { foodDays -> computeNutrientGrams(foodDays, NutrientType.Carbs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val fatListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .map { foodDays -> computeNutrientGrams(foodDays, NutrientType.Fat) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sugarListData = foodEntryRepository.loadFoodDayEntriesRangeFlow(startDate, endDate)
        .map { foodDays -> computeNutrientGrams(foodDays, NutrientType.AddedSugar) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


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

    private fun computeStepsList(list: List<DayEntry>): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        val byDay = list.associateBy { dayStartMillis(it.date) }
        return weekTemplate.map { slot ->
            val key = dayStartMillis(slot.startTime)
            BarGraphData(
                height = byDay[key]?.totalSteps?.toFloat() ?: 0f,
                x = slot.x,
                startTime = slot.startTime,
            )
        }
    }

    private fun computeEnergyExpList(list: List<DayEntry>): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        val byDay = list.associateBy { dayStartMillis(it.date) }
        return weekTemplate.map { slot ->
            val key = dayStartMillis(slot.startTime)
            BarGraphData(
                height = byDay[key]?.totalCalories ?: 0f,
                x = slot.x,
                startTime = slot.startTime,
            )
        }
    }

    private fun computeActiveEnergyList(entries: List<ActivityEntry>): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        val byDay = entries.groupBy { dayStartMillis(Date(it.startTime)) }
        return weekTemplate.map { slot ->
            val key = dayStartMillis(slot.startTime)
            BarGraphData(
                height = byDay[key]?.sumOf { it.calories }?.toFloat() ?: 0f,
                x = slot.x,
                startTime = slot.startTime,
            )
        }
    }

    private fun computeCalorieBalance(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<DayEntry>
    ): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        val foodByDay = foodDays.associateBy { it.day }
        val burnByDay = activityDays.associateBy { it.startTime }
        return weekTemplate.map { slot ->
            val key = dayStartMillis(slot.startTime)
            val expenditure = burnByDay[key]?.totalCalories ?: 0f
            val consumed = foodByDay[key]?.calories?.toFloat() ?: expenditure
            BarGraphData(consumed - expenditure, slot.x, slot.startTime)
        }
    }

    private fun computeWeightDelta(entries: List<WeightEntry>): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        if (entries.isEmpty()) {
            return weekTemplate.map { BarGraphData(0f, it.x, it.startTime) }
        }
        val sorted = entries.sortedBy { it.timeInMillis }
        val baseline = sorted.first().weightKg()
        return weekTemplate.map { slot ->
            val dayEnd = dayStartMillis(slot.startTime) + ONE_DAY_MILLIS
            val latest = sorted.lastOrNull { it.timeInMillis < dayEnd }?.weightKg() ?: baseline
            BarGraphData((latest - baseline).toFloat(), slot.x, slot.startTime)
        }
    }

    private fun computeProjectedWeightDelta(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<DayEntry>
    ): List<BarGraphData> {
        val balance = computeCalorieBalance(foodDays, activityDays)
        var cumulative = 0f
        return balance.map { point ->
            cumulative += point.height
            BarGraphData(cumulative / KCAL_PER_KG, point.x, point.startTime)
        }
    }

    private fun computeNutrientGrams(
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        type: NutrientType,
    ): List<BarGraphData> {
        val weekTemplate = getWeekTemplate()
        val byDay = foodDays.associateBy { it.day }
        return weekTemplate.map { slot ->
            val key = dayStartMillis(slot.startTime)
            val grams = byDay[key]?.nutrientInfo?.get(type)?.toFloat() ?: 0f
            BarGraphData(grams, slot.x, slot.startTime)
        }
    }

    private fun getWeekTemplate(): List<BarGraphData> {
        val list = mutableListOf<BarGraphData>()
        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            time = endDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        for (i in 0 until 7) {
            val date = calendar.time
            list.add(BarGraphData(0f, "", date))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list.asReversed()
    }

    private val calendarInstance = Calendar.getInstance(TimeZone.getDefault())

    fun dayStartMillis(d: Date): Long = synchronized(calendarInstance) {
        calendarInstance.apply {
            time = d
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
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
