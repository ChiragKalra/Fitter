package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.bruhascended.db.food.entities.DayEntry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.food.types.QuantityType
import com.bruhascended.fitapp.databinding.ItemCardNutritionBinding
import com.bruhascended.fitapp.util.AnimationDuration
import java.util.*


class NutritionCardPresenter (
    private val mContext: Context,
    mInflater: LayoutInflater,
) {

    private val binding = ItemCardNutritionBinding.inflate(mInflater)

    val view: View
    get() = binding.root

    private fun ItemCardNutritionBinding.presentSeparator(
        separatorInfo: DayEntry
    ) {
        textviewCalories.text = mContext.getString(
            com.bruhascended.db.R.string.calorie_count,
            separatorInfo.calories.toString()
        )

        // TODO: Set Using User Preference
        progressbarCalories.apply {
            progress = 0f
            progressMax = 1800f
            setProgressWithAnimation(
                separatorInfo.calories.toFloat(),
                AnimationDuration.VERY_LONG.ms
            )
        }

        separatorInfo.nutrientInfo.forEach { (type, value) ->
            if (type == null) return@forEach
            when (type) {
                NutrientType.Protein -> textviewProteinGram
                NutrientType.Carbs -> textviewCarbsGram
                NutrientType.Fat -> textviewFatGram
            }.text = QuantityType.Gram.toString(mContext, value)

            when (type) {
                NutrientType.Protein -> progressbarProtein
                NutrientType.Carbs -> progressbarCarbs
                NutrientType.Fat -> progressbarFat
            }.apply {
                // TODO: Set Using User Preference
                progressMax = 100f
                progress = 0f
                setProgressWithAnimation(value.toFloat(), AnimationDuration.VERY_LONG.ms)
            }

        }
    }

    private var previousValue: DayEntry? = null
    fun generateCard(values: DayEntry) {
        if (previousValue == values) return
        binding.presentSeparator(values)
        previousValue = values
    }
}
