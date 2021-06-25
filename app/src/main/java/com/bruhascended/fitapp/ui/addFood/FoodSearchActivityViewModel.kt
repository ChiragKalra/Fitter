package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Food
import com.bruhascended.fitapp.repository.FdaApi
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodSearchActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val db by FoodEntryRepository.Companion.Delegate(application)
    var error = MutableLiveData<String?>()
    val food_hints_list = MutableLiveData<List<Hint?>>()
    val food_history_list = MutableLiveData<MutableList<Food>>()


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
            .observeForever {
                food_history_list.postValue(it.toMutableList())
            }
    }

    private fun processData(hints: List<Hint>?) {
        food_hints_list.postValue(hints)
    }

    fun getError() = error.value
}