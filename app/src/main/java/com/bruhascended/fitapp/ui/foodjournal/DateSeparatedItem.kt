package com.bruhascended.fitapp.ui.foodjournal

import com.bruhascended.db.food.entities.FoodEntry
import java.util.*

class DateSeparatedItem(
    val item: FoodEntry? = null,
    val separator: Date? = null
) {

    val isSeparator: Boolean
        get() = separator != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DateSeparatedItem
        return if (isSeparator and other.isSeparator) {
            separator == other.separator
        } else {
            item == other.item
        }
    }

    override fun hashCode(): Int {
        var result = item?.hashCode() ?: 0
        result = 31 * result + (separator?.hashCode() ?: 0)
        return result
    }
}