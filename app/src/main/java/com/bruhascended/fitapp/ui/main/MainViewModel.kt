package com.bruhascended.fitapp.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.FoodEntryDatabaseFactory


class MainViewModel (
    private val mApp: Application
) : AndroidViewModel(mApp) {
    val foodEntryDb = FoodEntryDatabaseFactory(mApp)
        .allowMainThreadOperations(false)
        .build()
}