package com.bruhascended.fitapp.ui.dashboard

import androidx.annotation.StringRes
import com.bruhascended.db.R as DbR
import com.bruhascended.fitapp.R

/** Home dashboard blocks that can be reordered, hidden, or shown. */
enum class DashboardSection(val persistenceId: String, @StringRes val titleRes: Int) {
    SUMMARY_RING("summary_ring", R.string.dashboard_section_summary_ring),
    TODAY_STATS("today_stats", R.string.dashboard_section_today_stats),
    STEPS_WEEK("steps_week", R.string.dashboard_section_steps_week),
    ENERGY_WEEK("energy_week", R.string.dashboard_section_energy_week),
    ACTIVE_ENERGY("active_energy", R.string.dashboard_section_active_energy),
    CALORIE_BALANCE("calorie_balance", R.string.dashboard_section_calorie_balance),
    WEIGHT_DELTA("weight_delta", R.string.dashboard_section_weight_delta),
    WEIGHT_PROJECTION("weight_projection", R.string.dashboard_section_weight_projection),
    NUTRIENTS("nutrients", R.string.dashboard_section_nutrients),
    NUTRIENT_PROTEIN("nutrient_protein", DbR.string.protein),
    NUTRIENT_CARBS("nutrient_carbs", DbR.string.carbs),
    NUTRIENT_FAT("nutrient_fat", DbR.string.fat),
    NUTRIENT_ADDED_SUGAR("nutrient_added_sugar", DbR.string.added_sugar),
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

        fun parseWidthFractions(raw: String?): Map<DashboardSection, Float> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split(',')
                .mapNotNull { token ->
                    val parts = token.split(':', limit = 2)
                    if (parts.size != 2) return@mapNotNull null
                    val section = entries.find { it.persistenceId == parts[0].trim() }
                        ?: return@mapNotNull null
                    val width = parts[1].trim().toFloatOrNull()
                        ?: return@mapNotNull null
                    section to DashboardUiConfig.clampWidthFraction(width)
                }
                .toMap()
        }

        fun serializeWidthFractions(widthFractions: Map<DashboardSection, Float>): String =
            widthFractions
                .toSortedMap(compareBy { it.persistenceId })
                .entries
                .joinToString(",") { (section, width) ->
                    "${section.persistenceId}:${DashboardUiConfig.clampWidthFraction(width)}"
                }

        fun parseHeightScales(raw: String?): Map<DashboardSection, Float> {
            if (raw.isNullOrBlank()) return emptyMap()
            return raw.split(',')
                .mapNotNull { token ->
                    val parts = token.split(':', limit = 2)
                    if (parts.size != 2) return@mapNotNull null
                    val section = entries.find { it.persistenceId == parts[0].trim() }
                        ?: return@mapNotNull null
                    val height = parts[1].trim().toFloatOrNull()
                        ?: return@mapNotNull null
                    section to DashboardUiConfig.clampHeightScale(height)
                }
                .toMap()
        }

        fun serializeHeightScales(heightScales: Map<DashboardSection, Float>): String =
            heightScales
                .toSortedMap(compareBy { it.persistenceId })
                .entries
                .joinToString(",") { (section, height) ->
                    "${section.persistenceId}:${DashboardUiConfig.clampHeightScale(height)}"
                }
    }
}

data class DashboardUiConfig(
    val order: List<DashboardSection>,
    val hiddenIds: Set<DashboardSection>,
    val widthFractions: Map<DashboardSection, Float> = emptyMap(),
    val heightScales: Map<DashboardSection, Float> = emptyMap(),
    val gridSize: DashboardGridSize = DashboardGridSize.Default,
) {
    fun visibleOrdered(): List<DashboardSection> =
        order.filter { it !in hiddenIds }

    fun widthFor(section: DashboardSection): Float =
        clampWidthFraction(widthFractions[section] ?: DEFAULT_CARD_WIDTH_FRACTION)

    fun heightScaleFor(section: DashboardSection): Float {
        val defaultScale = when (section) {
            DashboardSection.SUMMARY_RING -> 1.25f
            DashboardSection.TODAY_STATS, DashboardSection.NUTRIENTS -> 0.75f
            else -> DEFAULT_CARD_HEIGHT_SCALE
        }
        return clampHeightScale(heightScales[section] ?: defaultScale)
    }

    companion object {
        const val MIN_CARD_WIDTH_FRACTION = 0.2f
        const val MAX_CARD_WIDTH_FRACTION = 1f
        const val DEFAULT_CARD_WIDTH_FRACTION = 0.5f
        const val DEFAULT_CARD_HEIGHT_SCALE = 1f
        const val MIN_CARD_HEIGHT_SCALE = 0.5f
        const val MAX_CARD_HEIGHT_SCALE = 1.75f

        val Default =
            DashboardUiConfig(DashboardSection.defaultOrdered, emptySet(), emptyMap(), emptyMap())

        fun clampWidthFraction(widthFraction: Float): Float =
            widthFraction.coerceIn(MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION)

        fun clampHeightScale(heightScale: Float): Float =
            heightScale.coerceIn(MIN_CARD_HEIGHT_SCALE, MAX_CARD_HEIGHT_SCALE)
    }
}

data class DashboardGridSize(
    val columns: Int,
    val heightUnits: Int,
) {
    val persistenceId: String = "${columns}x$heightUnits"

    companion object {
        val Default = DashboardGridSize(columns = 4, heightUnits = 4)
        val Options = listOf(
            Default,
            DashboardGridSize(columns = 4, heightUnits = 5),
            DashboardGridSize(columns = 4, heightUnits = 6),
            DashboardGridSize(columns = 5, heightUnits = 5),
        )

        fun parse(raw: String?): DashboardGridSize =
            Options.firstOrNull { it.persistenceId == raw } ?: Default
    }
}
