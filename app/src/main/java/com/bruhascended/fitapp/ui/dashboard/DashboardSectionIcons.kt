package com.bruhascended.fitapp.ui.dashboard

import androidx.annotation.DrawableRes
import com.bruhascended.fitapp.R

@DrawableRes
fun DashboardSection.listPreviewIcon(): Int =
    when (this) {
        DashboardSection.SUMMARY_RING -> R.drawable.ic_dashboard
        DashboardSection.TODAY_STATS -> R.drawable.ic_journal
        DashboardSection.STEPS_WEEK -> R.drawable.ic_steps
        DashboardSection.ENERGY_WEEK -> R.drawable.ic_energy_burn
        DashboardSection.ACTIVE_ENERGY -> R.drawable.ic_workout
        DashboardSection.CALORIE_BALANCE -> R.drawable.ic_trending_down
        DashboardSection.WEIGHT_DELTA,
        DashboardSection.WEIGHT_PROJECTION,
            -> R.drawable.ic_monitor_weight
        DashboardSection.NUTRIENTS -> R.drawable.ic_consumed
        DashboardSection.NUTRIENT_PROTEIN,
        DashboardSection.NUTRIENT_CARBS,
        DashboardSection.NUTRIENT_FAT,
        DashboardSection.NUTRIENT_ADDED_SUGAR,
            -> R.drawable.ic_consumed
    }
