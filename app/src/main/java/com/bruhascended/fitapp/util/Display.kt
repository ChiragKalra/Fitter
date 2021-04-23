package com.bruhascended.fitapp.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


public fun Context.toDP (px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

public fun Context.toPx (dp: Int): Float {
    return resources.displayMetrics.density * dp
}

fun AppCompatActivity.setupToolbar(
        toolbar: Toolbar, mTitle: String? = null, home : Boolean = true
) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(home)
        setDisplayShowHomeEnabled(home)
        title = mTitle ?: return
    }
}