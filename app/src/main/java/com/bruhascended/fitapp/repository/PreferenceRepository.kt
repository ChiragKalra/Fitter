package com.bruhascended.fitapp.repository

import android.app.Application
import androidx.preference.PreferenceManager

class PreferenceRepository(
    mApp: Application
) {

    companion object {
        const val PREF_GOAL_CALORIE_NET = "PREF_GOAL_CALORIE_NET"
        const val PREF_GOAL_CALORIE_CONSUMPTION = "PREF_GOAL_CALORIE_CONSUMPTION"
        const val PREF_GOAL_CALORIE_BURN = "PREF_GOAL_CALORIE_BURN"

        const val PREF_GOAL_PROTEIN = "PREF_GOAL_PROTEIN"
        const val PREF_GOAL_CARBS = "PREF_GOAL_CARBS"
        const val PREF_GOAL_FAT = "PREF_GOAL_FAT"

        const val PREF_GOAL_DISTANCE = "PREF_GOAL_DISTANCE"
        const val PREF_GOAL_DURATION = "PREF_GOAL_DURATION"
        const val PREF_GOAL_STEPS = "PREF_GOAL_STEPS"
    }

    private val mPrefManager = PreferenceManager.getDefaultSharedPreferences(mApp)

    operator fun get(key: String): Float? {
        val got = mPrefManager.getFloat(key, -1f)
        return if (got == -1f) null else got
    }

    operator fun set(key: String, value: Float) {
        mPrefManager.edit().putFloat(key, value).apply()
    }

}