package com.bruhascended.fitapp.ui.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * The floating card rendered above the LazyColumn while a drag is in progress.
 *
 * It's positioned at the finger location minus the original touch offset in the card,
 * scaled up slightly, and given a heavy shadow — matching the Android launcher's
 * "lifted widget" feel.
 */
@Composable
internal fun DashboardDragOverlay(
    dragState: DashboardDragState,
    cardWidthDp: Dp,
    cardHeightDp: Dp,
    parentTopInWindow: Float,
    content: @Composable () -> Unit,
) {
    if (dragState.draggedSection == null) return

    val density = LocalDensity.current

    val targetScale = when (dragState.phase) {
        DashboardDragState.Phase.LIFTING,
        DashboardDragState.Phase.DRAGGING -> 1.05f
        else -> 1f
    }
    val targetAlpha = when (dragState.phase) {
        DashboardDragState.Phase.LIFTING,
        DashboardDragState.Phase.DRAGGING -> 0.92f
        else -> 1f
    }
    val targetElevation = when (dragState.phase) {
        DashboardDragState.Phase.LIFTING,
        DashboardDragState.Phase.DRAGGING -> 12.dp
        else -> 0.dp
    }

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 800f),
        label = "drag_overlay_scale",
    )
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = spring(stiffness = 600f),
        label = "drag_overlay_alpha",
    )

    val shape = RoundedCornerShape(12.dp)

    // Position the overlay so the touch point stays under the finger
    val offsetX = with(density) { (dragState.fingerWindowOffset.x - dragState.touchOffsetInCard.x).toDp() }
    val offsetY = with(density) { (dragState.fingerWindowOffset.y - parentTopInWindow - dragState.touchOffsetInCard.y).toDp() }

    Box(
        modifier = Modifier
            .zIndex(Float.MAX_VALUE)
            .offset { IntOffset(offsetX.roundToPx(), offsetY.roundToPx()) }
            .width(cardWidthDp)
            .height(cardHeightDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .shadow(targetElevation, shape)
            .clip(shape),
    ) {
        content()
    }
}
