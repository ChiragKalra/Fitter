package com.bruhascended.db.activity

import android.content.Context
import androidx.room.Room

class ActivityEntryDatabaseFactory (
    private val mContext: Context
) {
    private var mainThread = false

    fun allowMainThreadOperations (bool: Boolean): ActivityEntryDatabaseFactory {
        mainThread = bool
        return this
    }

    fun build() = Room.databaseBuilder(
        mContext, ActivityEntryDatabase::class.java, "ActivityEntries"
    ).apply {
        if (mainThread) allowMainThreadQueries()
    }.build()
}