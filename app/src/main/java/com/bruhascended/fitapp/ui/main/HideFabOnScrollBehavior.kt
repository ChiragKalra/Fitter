package com.bruhascended.fitapp.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewPropertyAnimator
import androidx.annotation.Dimension
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.animation.AnimationUtils

/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * The [Behavior] for a View within a [CoordinatorLayout] to hide the view off the
 * bottom of the screen when scrolling down, and show it when scrolling up.
 */

class HideFabOnScrollBehavior<V : View> (context: Context, attrs: AttributeSet):
        CoordinatorLayout.Behavior<V>(context, attrs)
{
    companion object {
        private const val ENTER_ANIMATION_DURATION = 225
        private const val EXIT_ANIMATION_DURATION = 175

        private const val STATE_SCROLLED_DOWN = 1
        private const val STATE_SCROLLED_UP = 2
    }


    private var height = 0
    private var currentState = STATE_SCROLLED_UP
    private var additionalHiddenOffsetY = 0
    private var currentAnimator: ViewPropertyAnimator? = null

    override fun onLayoutChild(
            parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        val paramsCompat = child.layoutParams as MarginLayoutParams
        height = child.measuredHeight + paramsCompat.bottomMargin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    /**
     * Sets an additional offset for the y position used to hide the view.
     *
     * @param child the child view that is hidden by this behavior
     * @param offset the additional offset in pixels that should be added when the view slides away
     */
    fun setAdditionalHiddenOffsetY(child: V, @Dimension offset: Int) {
        additionalHiddenOffsetY = offset
        if (currentState == STATE_SCROLLED_DOWN) {
            child.translationX = (height + additionalHiddenOffsetY).toFloat()
        }
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V,
            directTargetChild: View,
            target: View,
            nestedScrollAxes: Int,
            type: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: V, target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int
    ) {
        if (dyConsumed > 0) {
            slideDown(child)
        } else if (dyConsumed < 0) {
            slideUp(child)
        }
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally on the
     * screen.
     */
    private fun slideUp(child: V) {
        if (currentState == STATE_SCROLLED_UP) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_UP
        animateChildTo(
                child, 0, ENTER_ANIMATION_DURATION.toLong(), AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
    }

    /**
     * Perform an animation that will slide the child from it's current position to be totally off the
     * screen.
     */
    private fun slideDown (child: V) {
        if (currentState == STATE_SCROLLED_DOWN) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_DOWN
        animateChildTo(
                child,
                height + additionalHiddenOffsetY,
                EXIT_ANIMATION_DURATION.toLong(),
                AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
    }

    private fun animateChildTo(
            child: V, targetY: Int, duration: Long, interpolator: TimeInterpolator) {
        currentAnimator = child
                .animate()
                .translationX(targetY.toFloat())
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                currentAnimator = null
                            }
                        })
    }
}
