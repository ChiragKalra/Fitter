package com.bruhascended.fitapp.util

import android.animation.Animator
import android.view.View

enum class AnimationDuration (
    public val ms: Long
) {
    LONG (700),
    MEDIUM (500),
    SHORT (350)
}

public fun View.animateRotation (
    degrees: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    animate()
        .setDuration(duration.ms)
        .rotation(degrees)
        .start()
}

public fun View.animateFadeUpIn (
    translation: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    alpha = 0f
    translationY = translation
    animate()
        .setDuration(duration.ms)
        .alpha(1f)
        .translationY(0f)
        .start()
}

public fun View.animateFadeIn (
    alp: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    alpha = 0f
    animate()
        .setDuration(duration.ms)
        .alpha(alp)
        .start()
}

public fun View.animateFadeDownOut (
    translation: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    translationY = 0f
    animate()
        .setDuration(duration.ms)
        .alpha(0f)
        .translationY(-translation)
        .start()
}

public fun View.animateFadeOut (
    duration: AnimationDuration = AnimationDuration.SHORT
) {
    animate()
        .setDuration(duration.ms)
        .alpha(0f)
        .start()
}