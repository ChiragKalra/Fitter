package com.bruhascended.fitapp.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HcNutritionLabelsTest {

    @Test
    fun keepsDistinctFoodTitles() {
        val label = nutritionDisplayLabelFromHcFields(rawName = "Protein Wafer Bar")
        assertEquals("Protein Wafer Bar", label)
    }

    @Test
    fun breakfastMealPlaceholderUsesReadableFallbackNotHexIdSuffix() {
        val label = nutritionDisplayLabelFromHcFields(
            rawName = "Breakfast",
            energyKcal = 804,
        )
        assertFalse(label.equals("Breakfast", ignoreCase = true))
        assertEquals("Imported meal · 804 kcal", label)
        assertFalse(label.contains("Food ·"))
        assertFalse(label.contains("…"))
    }

    @Test
    fun breakfastWithoutEnergyShowsShortFallback() {
        val label = nutritionDisplayLabelFromHcFields(rawName = "Breakfast", energyKcal = null)
        assertEquals("Imported meal", label)
    }

    @Test
    fun isMealSlotLabelRecognizesVariants() {
        assertTrue(isMealSlotLabel("BREAKFAST"))
        assertTrue(isMealSlotLabel("Evening snack"))
        assertFalse(isMealSlotLabel("Multigrain chips"))
    }

    @Test
    fun differentEnergyValuesDisambiguateIdenticalPlaceholderNames() {
        val energies = listOf(100, 200, 300)
        val labels =
            energies.map { kcal ->
                nutritionDisplayLabelFromHcFields(rawName = "Breakfast", energyKcal = kcal)
            }
        assertEquals(3, labels.toSet().size)
    }

    @Test
    fun aggregatesMultiFoodNameAcrossDelimitersIntoLines() {
        val raw = "Protein Wafer bar·Baked Beans\nBreakfast"
        val label = nutritionDisplayLabelFromHcFields(rawName = raw, energyKcal = 500)
        assertEquals(
            """Protein Wafer bar
Baked Beans""",
            label,
        )
    }

    @Test
    fun splitFoodNameSegmentsTrimsPieces() {
        assertEquals(
            listOf("A", "B", "C"),
            splitFoodNameSegments(" A ; B \n Breakfast · C ")
                .filterNot { isMealSlotLabel(it) },
        )
    }
}
