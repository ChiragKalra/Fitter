package com.bruhascended.fitapp.ui.addfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foods.Food
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AddFoodActivityViewModel : ViewModel() {

    private var _FoodName = MutableLiveData<String>()
    var FoodName: LiveData<String> = _FoodName

    private var _FoodEnergy = MutableLiveData<String>()
    var FoodEnergy: LiveData<String> = _FoodEnergy

    private var _EnergyUnit = MutableLiveData<String>()
    var EnergyUnit: LiveData<String> = _EnergyUnit

    val setDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val setTime: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    fun setData(food: Food) {
        CoroutineScope(IO).launch {
            for (i in food.foodNutrients) {
                if (i.nutrientId == 1008) {
                    _FoodName.postValue(food.description.lowercase())
                    _FoodEnergy.postValue(i.value.toString())
                    _EnergyUnit.postValue(i.unitName)
                    break
                }
            }
        }
    }
}