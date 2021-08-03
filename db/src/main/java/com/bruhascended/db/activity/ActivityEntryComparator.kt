package com.bruhascended.db.activity

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.activity.entities.ActivityEntry

object ActivityEntryComparator : DiffUtil.ItemCallback<ActivityEntry>() {
    override fun areItemsTheSame(oldItem: ActivityEntry, newItem: ActivityEntry) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ActivityEntry, newItem: ActivityEntry) =
        oldItem == newItem
}