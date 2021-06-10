package com.bruhascended.fitapp.util.datetime

import androidx.recyclerview.widget.DiffUtil


class DateSeparatedItemComparator<T> : DiffUtil.ItemCallback<DateSeparatedItem<T>>() {

    override fun areItemsTheSame (
        oldItem: DateSeparatedItem<T>,
        newItem: DateSeparatedItem<T>
    ) : Boolean {
        return if (oldItem.item == null && newItem.item == null) {
            oldItem.separator == newItem.separator
        } else if (oldItem.item != null && newItem.item != null) {
            oldItem.item.hashCode() == newItem.item.hashCode()
        } else {
            false
        }
    }

    override fun areContentsTheSame(
        oldItem: DateSeparatedItem<T>,
        newItem: DateSeparatedItem<T>
    ) = oldItem == newItem
}