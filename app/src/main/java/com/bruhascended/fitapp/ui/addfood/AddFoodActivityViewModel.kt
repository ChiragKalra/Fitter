package com.bruhascended.fitapp.ui.addfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Hint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddFoodActivityViewModel : ViewModel() {

    private var _per_kcal = MutableLiveData<Double>()
    var per_kcal: LiveData<Double> = _per_kcal

    private var item_map = HashMap<String?, Double?>()

    val default_types = arrayOf("Unit", "Gram", "Millilitre")  // extract as string resource
    private var _type_arr = MutableLiveData<List<String>>()
    var type_arr: LiveData<List<String>> = _type_arr

    private var _FoodName = MutableLiveData<String>()
    var FoodName: LiveData<String> = _FoodName

    private var _default_type = MutableLiveData<String>()
    var default_type: LiveData<String> = _default_type

    private var _default_quantity = MutableLiveData<String>()
    var default_quantity: LiveData<String> = _default_quantity

    private var _energy = MutableLiveData<String?>()
    var energy: LiveData<String?> = _energy


    val setDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    init {
        _default_type.postValue("Unit")
    }

    fun setData(hint: Hint?) {
        CoroutineScope(IO).launch {
            // Calculation
            val temp = mutableListOf<String>()
            for (m in hint?.measures!!) {
                if (checkout(m.label)) temp.add("${m.label}(${m.weight?.toInt()}g)")
            }
            for (i in hint.measures!!) {
                item_map[i.label.toString() + "(${i.weight?.toInt()}g)"] = i.weight
            }

            // Value Updating
            _default_quantity.postValue("100") // extract as string resource
            _default_type.postValue("Gram(1g)")
            _per_kcal.postValue(hint.food?.nutrients?.Energy!! / 100)
            _FoodName.postValue(hint.food?.label)
            _type_arr.postValue(temp)
        }
    }

    fun checkout(label: String?): Boolean {
        // extract as string resource
        if (label == "Kilogram" || label == "Ounce" || label == "Pound") return false
        return true
    }

    fun calculateEnergy(quantity: String, key: String?) {
        _energy.postValue(
            String.format(
                "%.2f", quantity.toDouble() * item_map[key]!!.toDouble() * _per_kcal.value!!
            )
        )
    }

    fun calculateEnergyOffline(quantity: String, per_kcal: String) {
        _energy.postValue(String.format("%.2f", (quantity.toDouble() * per_kcal.toDouble())))
    }
}