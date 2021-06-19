package com.bruhascended.fitapp.ui.addFoodv2

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.util.FoodNutrientDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class FoodDetailsActivityViewModel : ViewModel() {
    private val weightInfo_map = HashMap<QuantityType, Double>()
    private val nutrientInfo_map = HashMap<NutrientType, Double>()
    private lateinit var foodHint: Hint
    val NutrientDetails = MutableLiveData<FoodNutrientDetails>()
    val typeArrayItems = MutableLiveData<List<QuantityType>>()
    val setDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun setData(hint: Hint) {
        CoroutineScope(IO).launch {
            for (measure in hint.measures) {
                if (checkout(measure.label)) weightInfo_map[QuantityType.valueOf(measure.label)] =
                    measure.weight
            } // Creating weight Info hash map for food Db

            for (nutrient in NutrientType.values()) {
                nutrientInfo_map[nutrient] =
                    hint.food.nutrients.nutrientList[nutrient.ordinal]
            } // Creating nutrient Info hash map for food Db

            // update the quantity type drop down
            typeArrayItems.postValue(weightInfo_map.keys.toList())

            foodHint = hint
        }
    }

    private fun checkout(label: String): Boolean {
        for (value in QuantityType.values())
            if (value.toString() == label) return true
        return false
    }

    fun calculateNutrientData(foodDetails: FoodNutrientDetails) {
        val factor =
            weightInfo_map[foodDetails.quantityType]?.let { foodDetails.quantity?.times(it) }
        foodDetails.apply {
            Energy = factor?.times(foodHint.food.nutrients.Energy / 100.0)
            Protein = factor?.times(foodHint.food.nutrients.Protein / 100.0)
            Carbs = factor?.times(foodHint.food.nutrients.Carbs / 100.0)
            Fat = factor?.times(foodHint.food.nutrients.Fat / 100.0)
        }
        NutrientDetails.postValue(foodDetails)
    }
}