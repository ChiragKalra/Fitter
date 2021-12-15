package com.bruhascended.db.friends

import androidx.recyclerview.widget.DiffUtil
import com.bruhascended.db.friends.entities.Friend

object FriendComparator : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend) =
        oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: Friend, newItem: Friend) =
        oldItem == newItem
}
