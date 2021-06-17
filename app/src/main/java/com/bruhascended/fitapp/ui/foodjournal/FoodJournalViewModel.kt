package com.bruhascended.fitapp.ui.foodjournal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import com.bruhascended.db.food.types.MealType
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.repository.FoodEntryRepository
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

class FoodJournalViewModel (mApp: Application) : AndroidViewModel(mApp) {

    private val foodEntryRepository by FoodEntryRepository.Companion.Delegate(mApp)

    val foodEntries = foodEntryRepository.loadConsumedFoodEntries()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.writeEntry(
                Food(
                    "Apple",
                    .5,
                    QuantityType.Gram,
                ).apply {
                    nutrientInfo.putAll(
                        mapOf(
                            NutrientType.Fat to 10.0,
                            NutrientType.Fiber to 40.0,
                            NutrientType.Protein to 3.2,
                            NutrientType.Carbs to 20.5
                        )
                    )
                    weightInfo[QuantityType.Whole] = 100.0
                },
                Entry(
                    100.0,
                    2.0,
                    QuantityType.Whole,
                    MealType.Breakfast,
                    Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 10))
                    }.timeInMillis
                )
            )
            foodEntryRepository.writeEntry(
                Food(
                    "Mango",
                    .7,
                    QuantityType.Gram,
                ).apply {
                    nutrientInfo.putAll(
                        mapOf(
                            NutrientType.Fat to 20.0,
                            NutrientType.Fiber to 20.5,
                            NutrientType.Protein to 0.2,
                            NutrientType.Carbs to 40.5
                        )
                    )
                    weightInfo[QuantityType.Whole] = 120.0
                },
                Entry(
                    120.0*0.7,
                    1.0,
                    QuantityType.Whole,
                    MealType.Lunch,
                    Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 10))
                    }.timeInMillis
                )
            )
            foodEntryRepository.writeEntry(
                Food(
                    "Pizza",
                    500.0,
                    QuantityType.Whole,
                ).apply {
                    nutrientInfo.putAll(
                        mapOf(
                            NutrientType.Fat to 40.0,
                            NutrientType.Fiber to 50.5,
                            NutrientType.Protein to 0.0,
                            NutrientType.Carbs to 0.5
                        )
                    )
                    weightInfo[QuantityType.Whole] = 300.0
                },
                Entry(
                    750.0,
                    1.5,
                    QuantityType.Whole,
                    MealType.Dinner,
                    Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -Random.nextInt(0, 10))
                    }.timeInMillis
                )
            )
        }
    }

    fun deleteEntry(foodEntry: FoodEntry) {
        CoroutineScope(Dispatchers.IO).launch {
            foodEntryRepository.deleteEntry(foodEntry)
        }
    }
}