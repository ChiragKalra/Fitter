package com.bruhascended.fitapp.ui.foodjournal

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.food.entities.FoodEntry
import java.util.*

class DateSeparatedItem(
    val type: ItemType,
    val item: FoodEntry? = null,
    val separator: Date? = null,
) {

    enum class ItemType {
        Item, Separator, Footer
    }

    class Comparator : DiffUtil.ItemCallback<DateSeparatedItem>() {

        override fun areItemsTheSame (
            oldItem: DateSeparatedItem,
            newItem: DateSeparatedItem
        ) : Boolean {
            return if (oldItem.isSeparator && newItem.isSeparator) {
                oldItem.separator == newItem.separator
            } else {
                oldItem.item?.entry?.entryId == newItem.item?.entry?.entryId
            }
        }

        override fun areContentsTheSame(
            oldItem: DateSeparatedItem,
            newItem: DateSeparatedItem
        ) = oldItem == newItem
    }

    val isSeparator: Boolean
        get() = type == ItemType.Separator

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