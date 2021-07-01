package com.bruhascended.db.activity.types

import android.content.Context
import androidx.annotation.StringRes

import com.bruhascended.db.R.string.*

enum class ActivityType (
    @StringRes
    val stringRes: Int
) {
    Running(running);
    // TODO: add more activities

    fun getString(context: Context) = context.getString(stringRes)
}