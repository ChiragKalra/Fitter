package com.bruhascended.fitapp.ui.addfood

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Foodsv2Response
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.repository.FdaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class FoodSearchActivityViewModel : ViewModel() {

    var error = MutableLiveData<String?>()
    private val _food_hints_list = MutableLiveData<Foodsv2Response?>() // To be corrected
    val food_hints_list = MutableLiveData<List<Hint?>>()


    fun getFoodsv2(query: String) {
        CoroutineScope(IO).launch {
            try {
                val response = FdaApi.fetchFoodsv2(query)
                if (response.isSuccessful) processData(response.body()?.hints)
            } catch (e: Exception) {
                error.postValue(e.message)
            }
        }
    }

    fun processData(hints: List<Hint>?) {
        food_hints_list.postValue(hints)
    }

    fun getError() = error.value
}