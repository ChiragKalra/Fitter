package com.bruhascended.fitapp.ui.activityjournal

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.activity.entities.ActivityEntry
import java.util.*


class DateSeparatedItem(
    val item: ActivityEntry? = null,
    val separator: Date? = null
) {

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
