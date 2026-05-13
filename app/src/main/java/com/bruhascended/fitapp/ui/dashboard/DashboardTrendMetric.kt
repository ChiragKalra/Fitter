package com.bruhascended.fitapp.ui.dashboard

enum class DashboardTrendMetric(
    val id: String,
    val title: String,
    val unit: String,
) {
    ACTIVE_ENERGY("active_energy", "Active energy", "kcal"),
    CALORIE_BALANCE("calorie_balance", "Calorie balance", "kcal"),
    NET_WEIGHT("net_weight", "Net weight change", "kg"),
    WEIGHT_PROJECTION("weight_projection", "Projected vs logged weight", "kg"),
    STEPS("steps", "Steps", "steps"),
    ENERGY_BURNED("energy_burned", "Total expenditure", "Cal"),
    PROTEIN("protein", "Protein", "g"),
    CARBS("carbs", "Carbs", "g"),
    FAT("fat", "Fat", "g"),
    ADDED_SUGAR("added_sugar", "Added sugar", "g");

    companion object {
        fun fromId(id: String?): DashboardTrendMetric =
            entries.firstOrNull { it.id == id } ?: ACTIVE_ENERGY
    }
}

fun DashboardSection.trendMetric(): DashboardTrendMetric? =
    when (this) {
        DashboardSection.ACTIVE_ENERGY -> DashboardTrendMetric.ACTIVE_ENERGY
        DashboardSection.CALORIE_BALANCE -> DashboardTrendMetric.CALORIE_BALANCE
        DashboardSection.WEIGHT_DELTA -> DashboardTrendMetric.NET_WEIGHT
        DashboardSection.WEIGHT_PROJECTION -> DashboardTrendMetric.WEIGHT_PROJECTION
        DashboardSection.STEPS_WEEK -> DashboardTrendMetric.STEPS
        DashboardSection.ENERGY_WEEK -> DashboardTrendMetric.ENERGY_BURNED
        DashboardSection.NUTRIENT_PROTEIN -> DashboardTrendMetric.PROTEIN
        DashboardSection.NUTRIENT_CARBS -> DashboardTrendMetric.CARBS
        DashboardSection.NUTRIENT_FAT -> DashboardTrendMetric.FAT
        DashboardSection.NUTRIENT_ADDED_SUGAR -> DashboardTrendMetric.ADDED_SUGAR
        else -> null
    }
