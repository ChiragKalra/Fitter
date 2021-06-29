package com.bruhascended.fitapp.ui.foodjournal

import androidx.recyclerview.widget.DiffUtil

class DateSeparatedItemComparator : DiffUtil.ItemCallback<DateSeparatedItem>() {

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