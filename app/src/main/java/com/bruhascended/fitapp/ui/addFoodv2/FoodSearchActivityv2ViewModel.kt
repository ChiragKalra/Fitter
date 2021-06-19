package com.bruhascended.fitapp.ui.addFoodv2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Foodsv2Response
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.fitapp.repository.FdaApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodSearchActivityv2ViewModel: ViewModel() {
    var error = MutableLiveData<String?>()
    val food_hints_list = MutableLiveData<List<Hint?>>()


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

    private fun processData(hints: List<Hint>?) {
        food_hints_list.postValue(hints)
    }

    fun getError() = error.value
}