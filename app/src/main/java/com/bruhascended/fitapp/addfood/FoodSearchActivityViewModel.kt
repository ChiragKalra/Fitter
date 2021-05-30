package com.bruhascended.fitapp.addfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.FoodClient
import com.example.api.models.foods.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FoodSearchActivityViewModel : ViewModel() {

    private val _foods_list = MutableLiveData<List<Food>>()
    val foods_list: LiveData<List<Food>> = _foods_list

    init {
        getFoods()
    }

    fun getFoods() {
        CoroutineScope(IO).launch {
            val response = FoodClient.fdaApi.getFoods("Banana")
            _foods_list.postValue(response.foods)
        }
    }
}