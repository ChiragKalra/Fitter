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

    fun build(): FoodEntryDatabase = Room.databaseBuilder(
        mContext, FoodEntryDatabase::class.java, "FoodEntries"
    ).apply {
        addMigrations(
            FoodEntryDatabase.MIGRATION_1_2,
            FoodEntryDatabase.MIGRATION_2_3,
            FoodEntryDatabase.MIGRATION_3_4,
        )
        if (mainThread) allowMainThreadQueries()
    }.build()
}