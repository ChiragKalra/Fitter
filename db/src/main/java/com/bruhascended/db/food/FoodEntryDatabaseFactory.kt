package com.bruhascended.db.food

import android.content.Context
import androidx.room.Room

class FoodEntryDatabaseFactory (
    private val mContext: Context
) {
    fun with (allowMainThread: Boolean) = Room.databaseBuilder(
        mContext, FoodEntryDatabase::class.java, "FoodEntries"
    ).apply {
        if (allowMainThread) allowMainThreadQueries()
    }.build()
}