package com.bruhascended.db.food

import android.content.Context
import androidx.room.Room

class FoodEntryDatabaseFactory (
    private val mContext: Context
) {
    private var mainThread = false

    fun allowMainThreadOperations (bool: Boolean): FoodEntryDatabaseFactory {
        mainThread = bool
        return this
    }

    fun build() = Room.databaseBuilder(
        mContext, FoodEntryDatabase::class.java, "FoodEntries"
    ).apply {
        if (mainThread) allowMainThreadQueries()
    }.build()
}