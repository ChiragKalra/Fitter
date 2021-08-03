package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.fitapp.repository.FoodEntryRepository


class DashboardViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Delegate(mApp)

    fun getLastWeekDayEntries() = foodEntryRepository.loadLastWeek()

}