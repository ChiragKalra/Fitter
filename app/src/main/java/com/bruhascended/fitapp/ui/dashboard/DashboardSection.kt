package com.bruhascended.fitapp.ui.dashboard

import androidx.annotation.StringRes
import com.bruhascended.fitapp.R

/** Home dashboard blocks that can be reordered, hidden, or shown. */
enum class DashboardSection(val persistenceId: String, @StringRes val titleRes: Int) {
    SUMMARY_RING("summary_ring", R.string.dashboard_section_summary_ring),
    TODAY_STATS("today_stats", R.string.dashboard_section_today_stats),
    STEPS_WEEK("steps_week", R.string.dashboard_section_steps_week),
    ENERGY_WEEK("energy_week", R.string.dashboard_section_energy_week),
    NUTRIENTS("nutrients", R.string.dashboard_section_nutrients),
    ;

    companion object {

        /** Default order matches the historical layout before customization. */
        val defaultOrdered: List<DashboardSection>
            get() = entries.toList()

        fun parseOrder(raw: String?): List<DashboardSection> {
            if (raw.isNullOrBlank()) return defaultOrdered
            val ids = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val found = mutableListOf<DashboardSection>()
            val used = mutableSetOf<DashboardSection>()
            for (id in ids) {
                val sec = entries.find { it.persistenceId == id } ?: continue
                if (sec !in used) {
                    found += sec
                    used += sec
                }
            }
            for (s in entries) {
                if (s !in used) {
                    found += s
                    used += s
                }
            }
            return found
        }

        fun parseHidden(raw: String?): Set<DashboardSection> {
            if (raw.isNullOrBlank()) return emptySet()
            return raw.split(',')
                .map { it.trim() }
                .mapNotNull { id -> entries.find { it.persistenceId == id } }
                .toSet()
        }
    }
}

data class DashboardUiConfig(
    val order: List<DashboardSection>,
    val hiddenIds: Set<DashboardSection>,
) {
    fun visibleOrdered(): List<DashboardSection> =
        order.filter { it !in hiddenIds }

    companion object {
        val Default =
            DashboardUiConfig(DashboardSection.defaultOrdered, emptySet())
    }
}
