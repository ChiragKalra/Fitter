package com.bruhascended.fitapp.ui.dashboard

enum class DashboardTrendMetric(
    val id: String,
    val title: String,
    val unit: String,
) {
    ACTIVE_ENERGY("active_energy", "Active energy", "kcal"),
    CALORIE_BALANCE("calorie_balance", "Calorie balance", "kcal"),
    NET_WEIGHT("net_weight", "Net weight change", "kg"),
    WEIGHT_PROJECTION("weight_projection", "Projected vs logged weight", "kg");

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
        else -> null
    }
