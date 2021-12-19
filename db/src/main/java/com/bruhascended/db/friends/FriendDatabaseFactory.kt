package com.bruhascended.db.friends

import android.content.Context
import androidx.room.Room

class FriendDatabaseFactory (
    private val mContext: Context
) {
    private var mainThread = false

    fun allowMainThreadOperations (bool: Boolean): FriendDatabaseFactory {
        mainThread = bool
        return this
    }

    fun build() = Room.databaseBuilder(
        mContext, FriendDatabase::class.java, "Friends"
    ).apply {
        if (mainThread) allowMainThreadQueries()
    }.build()
}
