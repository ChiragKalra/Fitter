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
        R.color.blue_500,
        R.color.blue_200
    ),
    WeeklyProteinsConsumed(
        R.string.protein,
        R.color.purple_500,
        R.color.purple_200
    ),
    WeeklyCarbsConsumed(
        R.string.carbs,
        R.color.green_500,
        R.color.green_200
    ),
    WeeklyFatsConsumed(
        R.string.fat,
        R.color.red_500,
        R.color.red_200
    ),
    //WeeklyNutrientConsumed,
    WeeklyCaloriesBurnt(
        R.string.calories_burnt,
        R.color.blue_500,
        R.color.blue_200
    ),
    WeeklyDistanceCovered(
        R.string.distance,
        R.color.green_500,
        R.color.green_200
    ),
    WeeklyStepsTaken(
        R.string.steps,
        R.color.purple_500,
        R.color.purple_200
    ),
    WeeklyActiveTime(
        R.string.activity,
        R.color.red_500,
        R.color.red_200
    );
}
