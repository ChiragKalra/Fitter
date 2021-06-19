package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.util.FoodNutrientDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class FoodDetailsActivityViewModel(application: Application) : ViewModel() {
    private val db by FoodEntryRepository.Companion.Delegate(application)
    private val weightInfo_map = EnumMap<QuantityType, Double>(QuantityType::class.java)
    private val nutrientInfo_map = EnumMap<NutrientType, Double>(NutrientType::class.java)
    private lateinit var foodHint: Hint
    val foodName = MutableLiveData<String>()
    val NutrientDetails = MutableLiveData<FoodNutrientDetails>()
    val typeArrayItems = MutableLiveData<List<QuantityType>>()
    val setDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun setData(hint: Hint) {
        CoroutineScope(IO).launch {
            foodName.postValue(hint.food.label)
            for (measure in hint.measures) {
                if (checkout(measure.label)) weightInfo_map[QuantityType.valueOf(measure.label)] =
                    measure.weight
            } // Creating weight Info hash map for food Db

            for (nutrient in NutrientType.values()) {
                nutrientInfo_map[nutrient] =
                    hint.food.nutrients.nutrientList[nutrient.ordinal] / 100.0
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

    fun insertData(foodName: String) {
        CoroutineScope(IO).launch {
            val food = Food(
                foodName,
                foodHint.food.nutrients.Energy / 100.0,
                QuantityType.Cup, // TODO THIS COLUMN TO BE REMOBVED FROM FOOD DB
                weightInfo_map,
                nutrientInfo_map
            )
            val entry = Entry(
                NutrientDetails.value?.Energy!!,
                NutrientDetails.value?.quantity!!,
                NutrientDetails.value?.quantityType!!,
                NutrientDetails.value?.mealType!!,
                0 // TODO DATE TO BE CORRECTED
            )
            db.writeEntry(food, entry)
        }
    }
}