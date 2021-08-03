package com.bruhascended.fitapp.ui.dashboard

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.bruhascended.fitapp.R

enum class WeeklyCardType (
    @StringRes
    val title: Int,
    @ColorRes
    val titleColor: Int,
    @ColorRes
    val plotColor: Int,
) {
    // DailyActivityGoals,
    // DailyNutritionGoals,
    WeeklyCaloriesConsumed(
        R.string.calories_consumed,
        R.color.blue_700,
        R.color.blue_500
    ),
    WeeklyProteinsConsumed(
        R.string.protein,
        R.color.purple_700,
        R.color.purple_500
    ),
    WeeklyCarbsConsumed(
        R.string.carbs,
        R.color.green_700,
        R.color.green_500
    ),
    WeeklyFatsConsumed(
        R.string.fat,
        R.color.red_700,
        R.color.red_500
    ),
    //WeeklyNutrientConsumed,
    // WeeklyCaloriesBurnt,
    // WeeklyDistanceCovered,
    // WeeklyStepsTaken,
    // WeeklyActiveTime;
}
