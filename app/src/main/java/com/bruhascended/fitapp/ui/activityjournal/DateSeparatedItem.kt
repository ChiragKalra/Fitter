package com.bruhascended.fitapp.ui.activityjournal

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry
import java.util.*


class DateSeparatedItem(
    val type: ItemType,
    val item: ActivityEntry? = null,
    val separator: Date? = null,
    val liveDayEntry: LiveData<DayEntry?>? = null
) {

    enum class ItemType {
        Item, Separator, Footer
    }

    class Comparator: DiffUtil.ItemCallback<DateSeparatedItem>() {
        override fun areItemsTheSame (
            oldItem: DateSeparatedItem,
            newItem: DateSeparatedItem
        ) : Boolean {
            return if (oldItem.isSeparator && newItem.isSeparator) {
                oldItem.separator == newItem.separator
            } else {
                oldItem.item?.id == newItem.item?.id
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
