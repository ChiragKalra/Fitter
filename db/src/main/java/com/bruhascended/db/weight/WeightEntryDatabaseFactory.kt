package com.bruhascended.db.weight

import android.content.Context
import androidx.room.Room

class WeightEntryDatabaseFactory (
    private val mContext: Context
) {
    private var mainThread = false

    fun allowMainThreadOperations (bool: Boolean): WeightEntryDatabaseFactory {
        mainThread = bool
        return this
    }

    fun build() = Room.databaseBuilder(
        mContext, WeightEntryDatabase::class.java, "WeightEntries"
    ).apply {
        if (mainThread) allowMainThreadQueries()
    }.build()
}