package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import java.util.*


class DashboardViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)
    private val activityEntryRepository by ActivityEntryRepository.Delegate(mApp)

    fun getLastWeekDayEntries() = foodEntryRepository.loadLastWeek()

    fun getTodayLiveNutrition() = foodEntryRepository.loadLiveSeparator(
        Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time
    )

    fun getTodayLiveActivity() = activityEntryRepository.loadLiveSeparatorAt(
        Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
        }.time
    )

    fun getLastWeekActivityEntries() = activityEntryRepository.loadLastWeekDayEntries()

    fun getLastMonthActivityEntries() = activityEntryRepository.loadLastMonthDayEntries()
}