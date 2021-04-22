package com.bruhascended.fitapp.util

import android.content.Context


public fun Context.toDP (px: Float): Int {
    return (px / resources.displayMetrics.density).toInt()
}

public fun Context.toPx (dp: Int): Float {
    return resources.displayMetrics.density * dp
}