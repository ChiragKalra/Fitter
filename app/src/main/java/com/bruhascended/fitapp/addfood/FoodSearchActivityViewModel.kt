package com.bruhascended.fitapp.addfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bruhascended.fitapp.service.FdaApi
import com.bruhascended.api.models.foods.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.lang.Exception

class FoodSearchActivityViewModel : ViewModel() {

    var error = MutableLiveData<String?>()
    private val _foods_list = MutableLiveData<List<Food>>()
    val foods_list: LiveData<List<Food>> = _foods_list

    fun getFoods(query: String) {
        CoroutineScope(IO).launch {
            try {
                val response = FdaApi.fetchFoods(query)
                if (response.isSuccessful) _foods_list.postValue(response.body()?.foods)
            } catch (e: Exception) {
                error.postValue(e.message)
            }
        }
    }

    fun getError() = error.value
}