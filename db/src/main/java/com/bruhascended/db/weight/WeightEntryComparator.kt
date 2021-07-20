package com.bruhascended.db.weight

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.weight.entities.WeightEntry

object WeightEntryComparator : DiffUtil.ItemCallback<WeightEntry>() {
    override fun areItemsTheSame(oldItem: WeightEntry, newItem: WeightEntry) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: WeightEntry, newItem: WeightEntry) =
        oldItem == newItem
}