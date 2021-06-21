package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Food
import com.bruhascended.fitapp.repository.FdaApi
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.util.FoodHistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodSearchActivityViewModel(application: Application) : ViewModel() {
    private val db by FoodEntryRepository.Companion.Delegate(application)
    var error = MutableLiveData<String?>()
    val food_hints_list = MutableLiveData<List<Hint?>>()
    val food_history_list = MutableLiveData<List<Food>>()

    init {
        searchConsumedFood("%%")
    }


    fun getFoodsv2(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = FdaApi.fetchFoodsv2(query)
                if (response.isSuccessful) processData(response.body()?.hints)
            } catch (e: Exception) {
                error.postValue(e.message)
            }
        }
    }

    fun searchConsumedFood(query: String) {
        db.searchConsumedFood(query)
            .observeForever(Observer {
                food_history_list.postValue(it)
            })
    }

    private fun processData(hints: List<Hint>?) {
        food_hints_list.postValue(hints)
    }

    fun getError() = error.value
}