package com.bruhascended.fitapp.health

/**
 * Split a Health Connect NutritionRecord.name that may concatenate several foods using
 * newlines / bullets / middots / semicolons (Samsung-style bundles).
 */
internal fun splitFoodNameSegments(raw: String): List<String> {
    val out = mutableListOf<String>()
    val lineSplit = Regex("[\r\n]+")
    val inLineSplit = Regex("[;•·]")
    raw.trim().split(lineSplit).forEach { line ->
        if (line.isBlank()) return@forEach
        line.split(inLineSplit).forEach { piece ->
            val t = piece.trim()
            if (t.isNotEmpty()) out.add(t)
        }
    }
    return out
}

/**
 * Build a user-visible food title from Health Connect NutritionRecord inputs.
 *
 * Prefer the vendor-provided name; strip meal-slot placeholders; optionally join several
 * non-placeholder segments. When nothing usable remains, use a short fallback (no opaque ids).
 */
internal fun nutritionDisplayLabelFromHcFields(
    rawName: CharSequence?,
    energyKcal: Int? = null,
): String {
    val energyPos = energyKcal?.takeIf { it > 0 }
    val trimmed = rawName?.trim()?.toString()?.takeIf { it.isNotEmpty() }

    if (!trimmed.isNullOrBlank()) {
        val segments = splitFoodNameSegments(trimmed)
        val foods = segments
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filterNot { isMealSlotLabel(it) }
        when {
            foods.size >= 2 -> return foods.joinToString("\n")
            foods.size == 1 -> return foods.single()
        }
    }

    return importedFallbackTitle(energyPos)
}

private fun importedFallbackTitle(energyKcalPositive: Int?): String =
    if (energyKcalPositive != null) {
        "Imported meal · ${energyKcalPositive} kcal"
    } else {
        "Imported meal"
    }

internal fun isMealSlotLabel(name: String): Boolean =
    name.trim().lowercase() in MealSlotSynonyms

/** Synonyms HC apps use instead of actual food descriptions. */
private val MealSlotSynonyms = setOf(
    "breakfast", "brunch",
    "lunch",
    "dinner", "supper",
    "snack", "snacks", "evening snack", "tea",
    "meal", "foods", "food", "eating", "eating event",
)
