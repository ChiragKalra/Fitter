package com.bruhascended.db.food

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.food.entities.FoodEntry

object FoodEntryComparator : DiffUtil.ItemCallback<FoodEntry>() {
    override fun areItemsTheSame(oldItem: FoodEntry, newItem: FoodEntry) =
        oldItem.entry.entryId == newItem.entry.entryId

    override fun areContentsTheSame(oldItem: FoodEntry, newItem: FoodEntry) =
        oldItem == newItem
}