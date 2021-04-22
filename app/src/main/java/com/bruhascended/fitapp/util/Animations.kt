package com.bruhascended.fitapp.util

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat


enum class AnimationDuration(
        public val ms: Long
) {
    LONG(700),
    MEDIUM(500),
    SHORT(350)
}

public fun ImageView.animateTintColor(
        dest: Int, duration: AnimationDuration = AnimationDuration.SHORT
) {
    val color = ImageViewCompat.getImageTintList(this)!!.defaultColor
    ValueAnimator.ofArgb(color, dest).apply {
        addUpdateListener {
            imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        setDuration(duration.ms)
        start()
    }
}

public fun View.animateRotation(
        degrees: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    animate()
        .setDuration(duration.ms)
        .rotation(degrees)
        .start()
}

public fun View.animateFadeUpIn(
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

public fun View.animateFadeIn(
        alp: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    alpha = 0f
    animate()
        .setDuration(duration.ms)
        .alpha(alp)
        .start()
}

public fun View.animateFadeDownOut(
        translation: Float, duration: AnimationDuration = AnimationDuration.SHORT
) {
    translationY = 0f
    animate()
        .setDuration(duration.ms)
        .alpha(0f)
        .translationY(-translation)
        .start()
}

public fun View.animateFadeOut(
        duration: AnimationDuration = AnimationDuration.SHORT
) {
    animate()
        .setDuration(duration.ms)
        .alpha(0f)
        .start()
}