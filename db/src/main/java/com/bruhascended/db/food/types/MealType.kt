package com.bruhascended.db.food.types

import android.content.Context
import androidx.annotation.StringRes
import com.bruhascended.db.R.string.*
import java.util.*


enum class MealType (
    @StringRes
    val stringRes: Int,
    val hoursOfDay: IntRange? = null
) {
    Breakfast(breakfast, 5 until 11),
    Brunch(brunch, 11 until 1),
    Lunch(lunch, 1 until 3),
    EveningSnack(evening_snack, 4 until 7),
    Dinner(dinner, 7 until 11),
    LateNightSnack(late_night_snack, 11 until 4),
    Other(other_meal_type);

    companion object {
        fun getCurrentMealType(): MealType {
            return getMealTypeAtHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        }

        fun getMealTypeAt(timeInMillis: Long): MealType {
            return getMealTypeAtHour(
                Calendar.getInstance().apply {
                    setTimeInMillis(timeInMillis)
                }.get(Calendar.HOUR_OF_DAY)
            )
        }

        fun getMealTypeAtHour(hourOfDay: Int): MealType {
            values().forEach {
                if (it.hoursOfDay != null && hourOfDay in it.hoursOfDay) {
                    return it
                }
            }
            return Other
        }
    }

    fun getString(context: Context) = context.getString(stringRes)
}
