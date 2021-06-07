package com.bruhascended.fitapp.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


fun Context.toDP (px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

fun Context.toPx (dp: Int): Float {
    return resources.displayMetrics.density * dp
}
