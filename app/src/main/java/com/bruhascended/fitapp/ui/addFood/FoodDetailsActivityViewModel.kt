package com.bruhascended.fitapp.ui.addFood

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.bruhascended.api.models.foodsv2.Hint
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*

data class FoodNutrientDetails(
    var Energy: String? = null,
    var Carbs: String? = null,
    var Fat: String? = null,
    var Protein: String? = null,
)

class SharedActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val db by FoodEntryRepository.Delegate(application)

    val foodName = MutableLiveData<String>()
    val NutrientDetails = MutableLiveData<FoodNutrientDetails>()
    val QuantityTYpeItems = MutableLiveData<List<QuantityType>>()

    val weightInfo_map = EnumMap<QuantityType, Double>(QuantityType::class.java)
    val nutrientInfo_map = EnumMap<NutrientType, Double>(NutrientType::class.java)
    var perEnergy: Double? = null


    fun setData(hint: Hint) {
        foodName.postValue(hint.food.label)
        perEnergy = hint.food.nutrients.Energy / 100.0
        for (measure in hint.measures) {
            if (checkOut(measure.label)) weightInfo_map[measure.label?.let {
                QuantityType.valueOf(it)
            }] = measure.weight
        } // Creating weight Info map for food Db

        for (nutrient in NutrientType.values()) {
            hint.food.nutrients.nutrientList[nutrient.ordinal]?.div(100.0).let {
                if (it != null) nutrientInfo_map[nutrient] = it
            }

        } // Creating nutrient Info map for food Db

        // update the quantity type drop down
        QuantityTYpeItems.postValue(weightInfo_map.keys.toList())
    }

    fun setDataFromDb(food: Food) {
        foodName.postValue(food.foodName)
        perEnergy = food.calories
        weightInfo_map.putAll(food.weightInfo) // Creating weight Info map for food Db
        nutrientInfo_map.putAll(food.nutrientInfo) // Creating nutrient Info map for food Db

        // update the quantity type drop down
        QuantityTYpeItems.postValue(weightInfo_map.keys.toList())
    }

    fun checkOut(label: String?): Boolean {
        for (value in QuantityType.values())
            if (value.toString() == label) return true
        return false
    }

    fun calculateNutrientData(context: Context, quantity: Double, quantityType: QuantityType) {
        val factor = weightInfo_map[quantityType]?.times(quantity)
        val nutrientDetailsUtil = FoodNutrientDetails()
        factor?.let {
            nutrientDetailsUtil.Energy = QuantityType.doubleToString(it.times(perEnergy!!))
            nutrientInfo_map[NutrientType.Protein]?.let { it1 ->
                nutrientDetailsUtil.Protein = QuantityType.Gram.toString(context, it * it1)
            }
            nutrientInfo_map[NutrientType.Carbs]?.let { it1 ->
                nutrientDetailsUtil.Carbs = QuantityType.Gram.toString(context, it * it1)
            }
            nutrientInfo_map[NutrientType.Fat]?.let { it1 ->
                nutrientDetailsUtil.Fat = QuantityType.Gram.toString(context, it * it1)
            }
            NutrientDetails.postValue(nutrientDetailsUtil)
        }
    }

    fun insertData(food: Food, entry: Entry) {
        CoroutineScope(IO).launch { db.writeEntry(food, entry) }
    }

    fun deleteData(foodEntry: FoodEntry) {
        CoroutineScope(IO).launch { db.deleteEntry(foodEntry) }
    }
}