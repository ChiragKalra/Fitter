package com.bruhascended.fitapp.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

/**
 * Adds status-bar insets to this view's padding. Preserves padding from XML/layout.
 */
fun View.applyStatusBarPadding() {
    val baseLeft = paddingLeft
    val baseTop = paddingTop
    val baseRight = paddingRight
    val baseBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        v.updatePadding(
            left = baseLeft + bars.left,
            top = baseTop + bars.top,
            right = baseRight + bars.right,
            bottom = baseBottom + bars.bottom
        )
        insets
    }
    requestApplyInsetsWhenReady()
}

/**
 * Increments the top margin by the status bar height so the view sits below the status bar.
 * Preserves the layout's original top margin.
 */
fun View.applyStatusBarMarginTop() {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    val baseTopMargin = params.topMargin
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = baseTopMargin + topInset
        }
        insets
    }
    requestApplyInsetsWhenReady()
}

private fun View.requestApplyInsetsWhenReady() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        post { requestApplyInsets() }
    }
}
