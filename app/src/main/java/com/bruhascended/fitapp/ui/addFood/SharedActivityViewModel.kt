package com.bruhascended.fitapp.ui.addFood

import android.app.Application
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


class SharedActivityViewModel(application: Application) : ViewModel() {
    private val db by FoodEntryRepository.Companion.Delegate(application)
    private val weightInfo_map = EnumMap<QuantityType, Double>(QuantityType::class.java)
    private val nutrientInfo_map = EnumMap<NutrientType, Double>(NutrientType::class.java)
    val foodName = MutableLiveData<String>()
    var perEnergy: Double? = null
    val NutrientDetails = MutableLiveData<FoodNutrientDetails>()
    val typeArrayItems = MutableLiveData<List<QuantityType>>()
    var millis = Calendar.getInstance().apply {
        set(Calendar.MILLISECOND, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.HOUR_OF_DAY, 0)
    }.timeInMillis
    val setDate: String = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    fun setData(hint: Hint) {
        CoroutineScope(IO).launch {
            foodName.postValue(hint.food.label)
            perEnergy = hint.food.nutrients.Energy / 100.0
            for (measure in hint.measures) {
                if (checkout(measure.label)) weightInfo_map[measure.label?.let {
                    QuantityType.valueOf(it)
                }] = measure.weight
            } // Creating weight Info map for food Db

            for (nutrient in NutrientType.values()) {
                hint.food.nutrients.nutrientList[nutrient.ordinal]?.div(100.0).let {
                    if(it != null) nutrientInfo_map[nutrient] = it
                }

            } // Creating nutrient Info map for food Db

            // update the quantity type drop down
            typeArrayItems.postValue(weightInfo_map.keys.toList())
        }
    }

    fun setDataFromDb(food: Food) {
        foodName.postValue(food.foodName)
        perEnergy = food.calories
        weightInfo_map.putAll(food.weightInfo) // Creating weight Info map for food Db
        nutrientInfo_map.putAll(food.nutrientInfo) // Creating nutrient Info map for food Db

        // update the quantity type drop down
        typeArrayItems.postValue(weightInfo_map.keys.toList())


    }

    private fun checkout(label: String?): Boolean {
        for (value in QuantityType.values())
            if (value.toString() == label) return true
        return false
    }

    fun calculateNutrientData(foodDetails: FoodNutrientDetails) {
        val factor =
            weightInfo_map[foodDetails.quantityType]?.let { foodDetails.quantity?.times(it) }
        foodDetails.apply {
            Energy = factor?.times(perEnergy!!)
            Protein = nutrientInfo_map[NutrientType.Protein]?.let { factor?.times(it) }
            Carbs = nutrientInfo_map[NutrientType.Carbs]?.let { factor?.times(it) }
            Fat = nutrientInfo_map[NutrientType.Fat]?.let { factor?.times(it) }
        }
        NutrientDetails.postValue(foodDetails)
    }

    fun insertData(foodName: String) {
        CoroutineScope(IO).launch {
            val food = Food(
                foodName,
                perEnergy!!,
                weightInfo_map,
                nutrientInfo_map
            )
            val entry = Entry(
                NutrientDetails.value?.Energy!!.toInt(),
                NutrientDetails.value?.quantity!!,
                NutrientDetails.value?.quantityType!!,
                NutrientDetails.value?.mealType!!,
                millis
            )
            db.writeEntry(food, entry)
        }
    }

    fun insertCustomData(foodName: String, foodDetails: FoodNutrientDetails) {
        CoroutineScope(IO).launch {
            weightInfo_map[foodDetails.quantityType] = 1.0
            val nutritionList = foodDetails.getNutrientList()
            for (value in NutrientType.values()) {
                if (nutritionList[value.ordinal] != null) {
                    nutrientInfo_map[value] =
                        nutritionList[value.ordinal]?.div(foodDetails.quantity!!)
                }
            }

            val food =
                Food(
                    foodName,
                    foodDetails.Energy!!,
                    weightInfo_map,
                    nutrientInfo_map
                )
            val entry = Entry(
                foodDetails.Energy!!.toInt(),
                foodDetails.quantity!!,
                foodDetails.quantityType!!,
                foodDetails.mealType!!,
                millis
            )
            db.writeEntry(food, entry)
        }
    }


}