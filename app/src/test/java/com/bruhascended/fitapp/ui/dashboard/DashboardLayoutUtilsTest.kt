package com.bruhascended.fitapp.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

/**
 * JVM unit tests for pure layout utility functions.
 *
 * Tests cover:
 *  - [DashboardUiConfig.widthFor] default and stored-value behaviour
 *  - [gridSpanForWidth] on a 4-column grid
 *  - [widthForGridSpan] / [gridSpanForWidth] round-trip
 *  - [heightForGridUnits] snapping
 *  - [reorderVisibleInFullOrder] identity, hidden-section skipping, and out-of-bounds
 *
 * **Validates: Requirements 2.3, 2.4**
 */
class DashboardLayoutUtilsTest {

    // -------------------------------------------------------------------------
    // Private helpers — copies of private functions in DashboardFragment.kt
    // (same pattern as DashboardPreservationTest.kt)
    // -------------------------------------------------------------------------

    private fun gridSpanForWidth(widthFraction: Float, gridColumns: Int): Int {
        val clamped = DashboardUiConfig.clampWidthFraction(widthFraction)
        return (clamped * gridColumns)
            .roundToInt()
            .coerceIn(1, gridColumns)
    }

    private fun widthForGridSpan(span: Int, gridColumns: Int): Float =
        (span.coerceIn(1, gridColumns).toFloat() / gridColumns)
            .coerceIn(
                DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
                DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
            )

    private fun heightForGridUnits(heightScale: Float, heightUnits: Int): Float {
        val clamped = DashboardUiConfig.clampHeightScale(heightScale)
        val step = 1f / heightUnits.coerceAtLeast(1)
        return (clamped / step).roundToInt()
            .coerceAtLeast(1)
            .times(step)
            .coerceIn(
                DashboardUiConfig.MIN_CARD_HEIGHT_SCALE,
                DashboardUiConfig.MAX_CARD_HEIGHT_SCALE,
            )
    }

    // -------------------------------------------------------------------------
    // widthFor() — default value
    // -------------------------------------------------------------------------

    @Test
    fun `widthFor returns 0_5f when widthFractions is empty`() {
        val order = DashboardSection.defaultOrdered
        val config = DashboardUiConfig(
            order = order,
            hiddenIds = emptySet(),
            widthFractions = emptyMap(),
            heightScales = emptyMap(),
        )

        val width = config.widthFor(DashboardSection.SUMMARY_RING)

        assertEquals(
            "widthFor(SUMMARY_RING) must return DEFAULT_CARD_WIDTH_FRACTION (0.5f) when widthFractions is empty",
            0.5f,
            width,
        )
    }

    // -------------------------------------------------------------------------
    // widthFor() — stored value
    // -------------------------------------------------------------------------

    @Test
    fun `widthFor returns stored value when widthFractions contains the section`() {
        val order = DashboardSection.defaultOrdered
        val config = DashboardUiConfig(
            order = order,
            hiddenIds = emptySet(),
            widthFractions = mapOf(DashboardSection.SUMMARY_RING to 0.75f),
            heightScales = emptyMap(),
        )

        val width = config.widthFor(DashboardSection.SUMMARY_RING)

        assertEquals(
            "widthFor(SUMMARY_RING) must return the stored value 0.75f",
            0.75f,
            width,
        )
    }

    // -------------------------------------------------------------------------
    // gridSpanForWidth — 4-column grid
    // -------------------------------------------------------------------------

    @Test
    fun `gridSpanForWidth maps fractions to correct spans on 4-column grid`() {
        assertEquals("0.25f on 4 cols should be span 1", 1, gridSpanForWidth(0.25f, 4))
        assertEquals("0.5f on 4 cols should be span 2",  2, gridSpanForWidth(0.5f,  4))
        assertEquals("0.75f on 4 cols should be span 3", 3, gridSpanForWidth(0.75f, 4))
        assertEquals("1.0f on 4 cols should be span 4",  4, gridSpanForWidth(1.0f,  4))
    }

    // -------------------------------------------------------------------------
    // widthForGridSpan / gridSpanForWidth — round-trip
    // -------------------------------------------------------------------------

    @Test
    fun `gridSpanForWidth widthForGridSpan round-trip holds for all spans on 4-column grid`() {
        val cols = 4
        for (span in 1..cols) {
            val width = widthForGridSpan(span, cols)
            val recovered = gridSpanForWidth(width, cols)
            assertEquals(
                "Round-trip must hold: gridSpanForWidth(widthForGridSpan($span, $cols), $cols) == $span. " +
                    "widthForGridSpan returned $width, gridSpanForWidth returned $recovered",
                span,
                recovered,
            )
        }
    }

    // -------------------------------------------------------------------------
    // heightForGridUnits — snapping
    // -------------------------------------------------------------------------

    @Test
    fun `heightForGridUnits snaps values to nearest grid unit on 4-unit grid`() {
        assertEquals("1.0f should snap to 1.0f",  1.0f, heightForGridUnits(1.0f, 4))
        assertEquals("1.1f should snap to 1.0f",  1.0f, heightForGridUnits(1.1f, 4))
        assertEquals("1.3f should snap to 1.25f", 1.25f, heightForGridUnits(1.3f, 4))
    }

    // -------------------------------------------------------------------------
    // reorderVisibleInFullOrder — identity
    // -------------------------------------------------------------------------

    @Test
    fun `reorderVisibleInFullOrder returns original list when fromVisible equals toVisible`() {
        val order = DashboardSection.defaultOrdered
        val hiddenIds = emptySet<DashboardSection>()

        for (i in order.indices) {
            val result = reorderVisibleInFullOrder(order, hiddenIds, i, i)
            assertEquals(
                "Identity move (from=$i, to=$i) must return the original list unchanged",
                order,
                result,
            )
        }
    }

    // -------------------------------------------------------------------------
    // reorderVisibleInFullOrder — with hidden sections
    // -------------------------------------------------------------------------

    @Test
    fun `reorderVisibleInFullOrder skips hidden sections when moving a visible section`() {
        // Arrange: interleave hidden sections between visible ones
        val order = listOf(
            DashboardSection.SUMMARY_RING,    // visible  — visible index 0
            DashboardSection.NUTRIENTS,        // hidden
            DashboardSection.TODAY_STATS,      // visible  — visible index 1
            DashboardSection.STEPS_WEEK,       // visible  — visible index 2
            DashboardSection.NUTRIENT_PROTEIN, // hidden
            DashboardSection.ENERGY_WEEK,      // visible  — visible index 3
        )
        val hiddenIds = setOf(DashboardSection.NUTRIENTS, DashboardSection.NUTRIENT_PROTEIN)

        // Move visible index 0 (SUMMARY_RING) to visible index 2 (STEPS_WEEK position)
        val result = reorderVisibleInFullOrder(order, hiddenIds, fromVisible = 0, toVisible = 2)

        // The visible subsequence after the move should be:
        // [TODAY_STATS, STEPS_WEEK, SUMMARY_RING, ENERGY_WEEK]
        val resultVisible = result.filter { it !in hiddenIds }
        val expectedVisible = listOf(
            DashboardSection.TODAY_STATS,
            DashboardSection.STEPS_WEEK,
            DashboardSection.SUMMARY_RING,
            DashboardSection.ENERGY_WEEK,
        )
        assertEquals(
            "Visible subsequence must reflect the move, skipping hidden sections",
            expectedVisible,
            resultVisible,
        )

        // The full list must still contain all original sections (no additions/removals)
        assertEquals("Result must have same size as original order", order.size, result.size)
        assertEquals("Result must contain same elements as original order", order.toSet(), result.toSet())
    }

    // -------------------------------------------------------------------------
    // reorderVisibleInFullOrder — out-of-bounds
    // -------------------------------------------------------------------------

    @Test
    fun `reorderVisibleInFullOrder returns original list when indices are out of bounds`() {
        val order = DashboardSection.defaultOrdered
        val hiddenIds = emptySet<DashboardSection>()
        val visibleCount = order.size // all visible

        // fromVisible out of bounds (negative)
        val result1 = reorderVisibleInFullOrder(order, hiddenIds, fromVisible = -1, toVisible = 0)
        assertEquals(
            "fromVisible=-1 (out of bounds) must return original list unchanged",
            order,
            result1,
        )

        // toVisible out of bounds (too large)
        val result2 = reorderVisibleInFullOrder(order, hiddenIds, fromVisible = 0, toVisible = visibleCount + 10)
        assertEquals(
            "toVisible=${visibleCount + 10} (out of bounds) must return original list unchanged",
            order,
            result2,
        )

        // Both out of bounds
        val result3 = reorderVisibleInFullOrder(order, hiddenIds, fromVisible = -5, toVisible = visibleCount + 5)
        assertEquals(
            "Both indices out of bounds must return original list unchanged",
            order,
            result3,
        )
    }
}
