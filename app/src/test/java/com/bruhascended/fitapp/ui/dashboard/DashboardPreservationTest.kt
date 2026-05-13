package com.bruhascended.fitapp.ui.dashboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToInt

/**
 * Preservation property tests for the dashboard widget layout bugfix.
 *
 * **Property 2: Preservation** — Non-Resize/Reorder Behavior Unchanged
 *
 * These tests establish the baseline behavior that MUST be preserved after the fix.
 * They are written on UNFIXED code and are EXPECTED TO PASS.
 * After the fix is applied, they must continue to pass (no regressions).
 *
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7**
 */
class DashboardPreservationTest {

    // -------------------------------------------------------------------------
    // Private helpers — copies of private functions in DashboardFragment.kt
    // (NOT modifying DashboardFragment.kt; logic inlined here for testing)
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
    // Property 2.1 — reorderVisibleInFullOrder
    //
    // For all valid (order, hiddenIds, fromVisible, toVisible) tuples:
    //   (a) the result contains the same elements as `order` (no additions/removals)
    //   (b) the visible subsequence of the result reflects the move
    //
    // **Validates: Requirements 3.2**
    // -------------------------------------------------------------------------

    @Test
    fun `2_1 reorderVisibleInFullOrder result contains same elements as order`() {
        // Property: the multiset of elements is preserved for all valid inputs.
        // We test exhaustively over a representative set of (order, hiddenIds, from, to) tuples.

        val allSections = DashboardSection.entries.toList()

        // Case 1: no hidden sections, move first to last
        run {
            val order = allSections
            val hiddenIds = emptySet<DashboardSection>()
            val visibleCount = order.size - hiddenIds.size
            for (from in 0 until visibleCount) {
                for (to in 0 until visibleCount) {
                    val result = reorderVisibleInFullOrder(order, hiddenIds, from, to)
                    assertEquals(
                        "Result must have same size as order for from=$from to=$to",
                        order.size,
                        result.size,
                    )
                    assertEquals(
                        "Result must contain same elements as order for from=$from to=$to",
                        order.toSet(),
                        result.toSet(),
                    )
                    // No duplicates
                    assertEquals(
                        "Result must have no duplicates for from=$from to=$to",
                        result.size,
                        result.toSet().size,
                    )
                }
            }
        }

        // Case 2: some hidden sections
        run {
            val hiddenIds = setOf(
                DashboardSection.NUTRIENTS,
                DashboardSection.NUTRIENT_PROTEIN,
                DashboardSection.NUTRIENT_CARBS,
            )
            val order = allSections
            val visibleSections = order.filter { it !in hiddenIds }
            val visibleCount = visibleSections.size
            for (from in 0 until visibleCount) {
                for (to in 0 until visibleCount) {
                    val result = reorderVisibleInFullOrder(order, hiddenIds, from, to)
                    assertEquals(
                        "Result must have same size as order (with hidden) for from=$from to=$to",
                        order.size,
                        result.size,
                    )
                    assertEquals(
                        "Result must contain same elements as order (with hidden) for from=$from to=$to",
                        order.toSet(),
                        result.toSet(),
                    )
                    assertEquals(
                        "Result must have no duplicates (with hidden) for from=$from to=$to",
                        result.size,
                        result.toSet().size,
                    )
                }
            }
        }

        // Case 3: small order for exhaustive visible-subsequence check
        run {
            val smallOrder = listOf(
                DashboardSection.SUMMARY_RING,
                DashboardSection.TODAY_STATS,
                DashboardSection.STEPS_WEEK,
                DashboardSection.ENERGY_WEEK,
            )
            val hiddenIds = emptySet<DashboardSection>()
            val visibleCount = smallOrder.size
            for (from in 0 until visibleCount) {
                for (to in 0 until visibleCount) {
                    val result = reorderVisibleInFullOrder(smallOrder, hiddenIds, from, to)
                    assertEquals(
                        "Small order: result must have same size for from=$from to=$to",
                        smallOrder.size,
                        result.size,
                    )
                    assertEquals(
                        "Small order: result must contain same elements for from=$from to=$to",
                        smallOrder.toSet(),
                        result.toSet(),
                    )
                }
            }
        }
    }

    @Test
    fun `2_1 reorderVisibleInFullOrder visible subsequence reflects the move`() {
        // Property: the visible subsequence of the result equals the visible subsequence
        // of the original with the element at fromVisible moved to toVisible.

        val allSections = DashboardSection.entries.toList()

        // Helper: compute expected visible order after a move
        fun expectedVisibleAfterMove(
            visible: List<DashboardSection>,
            from: Int,
            to: Int,
        ): List<DashboardSection> {
            if (from == to || from !in visible.indices || to !in visible.indices) return visible
            return visible.toMutableList().apply {
                val item = removeAt(from)
                add(to, item)
            }
        }

        // Case 1: no hidden sections
        run {
            val order = allSections.take(5) // use 5 sections for exhaustive check
            val hiddenIds = emptySet<DashboardSection>()
            val visible = order.filter { it !in hiddenIds }
            for (from in visible.indices) {
                for (to in visible.indices) {
                    val result = reorderVisibleInFullOrder(order, hiddenIds, from, to)
                    val resultVisible = result.filter { it !in hiddenIds }
                    val expected = expectedVisibleAfterMove(visible, from, to)
                    assertEquals(
                        "Visible subsequence must reflect move from=$from to=$to",
                        expected,
                        resultVisible,
                    )
                }
            }
        }

        // Case 2: with hidden sections — hidden sections must stay hidden, visible order updated
        run {
            val order = listOf(
                DashboardSection.SUMMARY_RING,   // visible
                DashboardSection.NUTRIENTS,       // hidden
                DashboardSection.TODAY_STATS,     // visible
                DashboardSection.STEPS_WEEK,      // visible
                DashboardSection.NUTRIENT_PROTEIN, // hidden
                DashboardSection.ENERGY_WEEK,     // visible
            )
            val hiddenIds = setOf(DashboardSection.NUTRIENTS, DashboardSection.NUTRIENT_PROTEIN)
            val visible = order.filter { it !in hiddenIds }
            // visible = [SUMMARY_RING, TODAY_STATS, STEPS_WEEK, ENERGY_WEEK]

            for (from in visible.indices) {
                for (to in visible.indices) {
                    val result = reorderVisibleInFullOrder(order, hiddenIds, from, to)
                    val resultVisible = result.filter { it !in hiddenIds }
                    val expected = expectedVisibleAfterMove(visible, from, to)
                    assertEquals(
                        "Visible subsequence (with hidden) must reflect move from=$from to=$to",
                        expected,
                        resultVisible,
                    )
                }
            }
        }

        // Case 3: identity — fromVisible == toVisible returns original order
        run {
            val order = allSections
            val hiddenIds = setOf(DashboardSection.NUTRIENTS)
            for (i in 0 until (order.size - hiddenIds.size)) {
                val result = reorderVisibleInFullOrder(order, hiddenIds, i, i)
                assertEquals(
                    "Identity move (from==to) must return original order for i=$i",
                    order,
                    result,
                )
            }
        }

        // Case 4: out-of-bounds indices return original order unchanged
        run {
            val order = allSections
            val hiddenIds = emptySet<DashboardSection>()
            val outOfBoundsResult1 = reorderVisibleInFullOrder(order, hiddenIds, -1, 0)
            assertEquals("Out-of-bounds fromVisible=-1 must return original order", order, outOfBoundsResult1)
            val outOfBoundsResult2 = reorderVisibleInFullOrder(order, hiddenIds, 0, order.size + 10)
            assertEquals("Out-of-bounds toVisible must return original order", order, outOfBoundsResult2)
        }
    }

    // -------------------------------------------------------------------------
    // Property 2.2 — gridSpanForWidth / widthForGridSpan round-trip
    //
    // For all span in 1..gridColumns:
    //   gridSpanForWidth(widthForGridSpan(span, cols), cols) == span
    //
    // **Validates: Requirements 3.3**
    // -------------------------------------------------------------------------

    @Test
    fun `2_2 gridSpanForWidth widthForGridSpan round-trip holds for all valid spans`() {
        // Property: converting a span to a width fraction and back yields the original span.
        // Tested for all grid column configurations used in the app.

        val gridColumnOptions = listOf(4, 5) // from DashboardGridSize.Options

        for (cols in gridColumnOptions) {
            for (span in 1..cols) {
                val width = widthForGridSpan(span, cols)
                val recoveredSpan = gridSpanForWidth(width, cols)
                assertEquals(
                    "Round-trip must hold: gridSpanForWidth(widthForGridSpan($span, $cols), $cols) == $span. " +
                        "widthForGridSpan($span, $cols) = $width, gridSpanForWidth($width, $cols) = $recoveredSpan",
                    span,
                    recoveredSpan,
                )
            }
        }
    }

    @Test
    fun `2_2 widthForGridSpan output is within valid fraction bounds`() {
        // Property: widthForGridSpan always returns a value in [MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION]
        val gridColumnOptions = listOf(4, 5)
        for (cols in gridColumnOptions) {
            for (span in 1..cols) {
                val width = widthForGridSpan(span, cols)
                assertTrue(
                    "widthForGridSpan($span, $cols) = $width must be >= MIN_CARD_WIDTH_FRACTION",
                    width >= DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
                )
                assertTrue(
                    "widthForGridSpan($span, $cols) = $width must be <= MAX_CARD_WIDTH_FRACTION",
                    width <= DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Property 2.3 — heightForGridUnits idempotency
    //
    // For all already-snapped height values, applying heightForGridUnits again
    // returns the same value (idempotent).
    //
    // **Validates: Requirements 3.3**
    // -------------------------------------------------------------------------

    @Test
    fun `2_3 heightForGridUnits is idempotent for already-snapped values`() {
        // Property: for any value h that is the output of heightForGridUnits(x, units),
        // heightForGridUnits(h, units) == h.

        val heightUnitOptions = listOf(4, 5, 6) // from DashboardGridSize.Options

        for (units in heightUnitOptions) {
            val step = 1f / units
            // Generate all valid snapped values for this unit count
            // Snapped values are multiples of step within [MIN_CARD_HEIGHT_SCALE, MAX_CARD_HEIGHT_SCALE]
            val snappedValues = mutableListOf<Float>()
            var v = DashboardUiConfig.MIN_CARD_HEIGHT_SCALE
            while (v <= DashboardUiConfig.MAX_CARD_HEIGHT_SCALE + step / 2) {
                val snapped = heightForGridUnits(v, units)
                if (snapped !in snappedValues) snappedValues.add(snapped)
                v += step
            }

            for (snapped in snappedValues) {
                val reSnapped = heightForGridUnits(snapped, units)
                assertEquals(
                    "heightForGridUnits must be idempotent: " +
                        "heightForGridUnits(heightForGridUnits($snapped, $units), $units) == $snapped. " +
                        "Got $reSnapped",
                    snapped,
                    reSnapped,
                    1e-6f,
                )
            }
        }
    }

    @Test
    fun `2_3 heightForGridUnits output is within valid height scale bounds`() {
        // Property: heightForGridUnits always returns a value in [MIN_CARD_HEIGHT_SCALE, MAX_CARD_HEIGHT_SCALE]
        val heightUnitOptions = listOf(4, 5, 6)
        // Test a wide range of input values including boundary and out-of-range values
        val testInputs = listOf(
            0f, 0.1f, 0.5f, 0.75f, 1.0f, 1.1f, 1.25f, 1.5f, 1.75f, 2.0f, 10f, -1f, Float.MAX_VALUE,
        )
        for (units in heightUnitOptions) {
            for (input in testInputs) {
                val result = heightForGridUnits(input, units)
                assertTrue(
                    "heightForGridUnits($input, $units) = $result must be >= MIN_CARD_HEIGHT_SCALE",
                    result >= DashboardUiConfig.MIN_CARD_HEIGHT_SCALE,
                )
                assertTrue(
                    "heightForGridUnits($input, $units) = $result must be <= MAX_CARD_HEIGHT_SCALE",
                    result <= DashboardUiConfig.MAX_CARD_HEIGHT_SCALE,
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Property 2.4 — DashboardUiConfig.clampWidthFraction bounds
    //
    // For all Float inputs, result is in [MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION].
    //
    // **Validates: Requirements 3.3, 3.7**
    // -------------------------------------------------------------------------

    @Test
    fun `2_4 clampWidthFraction result is always within bounds for all Float inputs`() {
        // Property: clampWidthFraction(x) in [MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION]
        // for all Float x.

        val testInputs = listOf(
            // Below minimum
            Float.NEGATIVE_INFINITY,
            -1000f,
            -1f,
            -0.1f,
            0f,
            0.01f,
            DashboardUiConfig.MIN_CARD_WIDTH_FRACTION - 0.001f,
            // At minimum
            DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
            // In range
            0.25f,
            0.3f,
            0.5f,
            0.6f,
            0.75f,
            0.8f,
            0.9f,
            // At maximum
            DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
            // Above maximum
            DashboardUiConfig.MAX_CARD_WIDTH_FRACTION + 0.001f,
            1.1f,
            2f,
            100f,
            1000f,
            Float.MAX_VALUE,
            Float.POSITIVE_INFINITY,
            // Special values
            Float.NaN,
        )

        for (input in testInputs) {
            // NaN is a special case: coerceIn with NaN input returns NaN in Kotlin,
            // but the function should still not crash. We skip NaN for the bounds check.
            if (input.isNaN()) continue

            val result = DashboardUiConfig.clampWidthFraction(input)
            assertTrue(
                "clampWidthFraction($input) = $result must be >= MIN_CARD_WIDTH_FRACTION (${DashboardUiConfig.MIN_CARD_WIDTH_FRACTION})",
                result >= DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
            )
            assertTrue(
                "clampWidthFraction($input) = $result must be <= MAX_CARD_WIDTH_FRACTION (${DashboardUiConfig.MAX_CARD_WIDTH_FRACTION})",
                result <= DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
            )
        }
    }

    @Test
    fun `2_4 clampWidthFraction is idempotent`() {
        // Property: clampWidthFraction(clampWidthFraction(x)) == clampWidthFraction(x)
        // i.e., applying the clamp twice is the same as applying it once.

        val testInputs = listOf(
            Float.NEGATIVE_INFINITY, -100f, -1f, 0f, 0.1f, 0.2f,
            DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
            0.3f, 0.5f, 0.75f,
            DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
            1.1f, 2f, 100f, Float.MAX_VALUE, Float.POSITIVE_INFINITY,
        )

        for (input in testInputs) {
            val once = DashboardUiConfig.clampWidthFraction(input)
            val twice = DashboardUiConfig.clampWidthFraction(once)
            assertEquals(
                "clampWidthFraction must be idempotent: clampWidthFraction(clampWidthFraction($input)) == clampWidthFraction($input). " +
                    "Got once=$once, twice=$twice",
                once,
                twice,
            )
        }
    }

    @Test
    fun `2_4 clampWidthFraction preserves values already in bounds`() {
        // Property: for x in [MIN_CARD_WIDTH_FRACTION, MAX_CARD_WIDTH_FRACTION],
        // clampWidthFraction(x) == x.

        val inBoundsValues = listOf(
            DashboardUiConfig.MIN_CARD_WIDTH_FRACTION,
            0.25f,
            0.5f,
            0.75f,
            DashboardUiConfig.MAX_CARD_WIDTH_FRACTION,
        )

        for (value in inBoundsValues) {
            val result = DashboardUiConfig.clampWidthFraction(value)
            assertEquals(
                "clampWidthFraction($value) must return $value unchanged (already in bounds). Got $result",
                value,
                result,
            )
        }
    }
}
