package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class FoodSearchViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodSearchActivityViewModel::class.java))
            return FoodSearchActivityViewModel(application) as T
        throw IllegalArgumentException("Invalid ViewModel Class")
    }
}