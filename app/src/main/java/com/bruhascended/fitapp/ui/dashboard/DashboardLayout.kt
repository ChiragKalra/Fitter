package com.bruhascended.fitapp.ui.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.bruhascended.fitapp.R

/**
 * Row-packing helper: given an ordered list of sections and a width function (0..1 fraction),
 * pack them into rows where each row's total width ≤ 1.
 */
internal fun packIntoRows(
    sections: List<DashboardSection>,
    widthOf: (DashboardSection) -> Float,
): List<List<DashboardSection>> {
    val rows = mutableListOf<List<DashboardSection>>()
    val currentRow = mutableListOf<DashboardSection>()
    var rowWidth = 0f
    for (section in sections) {
        val w = widthOf(section).coerceIn(0.01f, 1f)
        if (currentRow.isNotEmpty() && rowWidth + w > 1f + 0.01f) {
            rows.add(currentRow.toList())
            currentRow.clear()
            rowWidth = 0f
        }
        currentRow.add(section)
        rowWidth += w
    }
    if (currentRow.isNotEmpty()) rows.add(currentRow.toList())
    return rows
}

/**
 * A single dashboard widget card.
 *
 * - Normal mode: tappable → navigates to trend detail.
 * - Drag-to-reorder: long-press lifts the card. Drag tracking is handled by a parent-level
 *   pointer handler (not here — avoids gesture death on recomposition).
 * - Resize mode (selected): shows resize handles on the card edges.
 */
@Composable
internal fun DashboardWidgetCard(
    section: DashboardSection,
    isBeingDragged: Boolean,
    selected: Boolean,
    committedWidthFraction: Float,
    committedHeightScale: Float,
    gridColumns: Int,
    heightUnits: Int,
    heightDp: Dp,
    cardBounds: MutableMap<DashboardSection, Rect>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onLiveResize: (snappedWidth: Float, snappedHeight: Float) -> Unit,
    onShapeChangeFinished: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val view = LocalView.current

    var draftWidth by remember(committedWidthFraction) { mutableFloatStateOf(committedWidthFraction) }
    var draftHeightScale by remember(committedHeightScale) { mutableFloatStateOf(committedHeightScale) }

    val baseHeight = 180.dp
    var containerWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val baseHeightPx = remember(density) { with(density) { baseHeight.toPx() }.coerceAtLeast(1f) }
    val fullRowWidthPx = (containerWidthPx / committedWidthFraction.coerceAtLeast(0.01f))

    val currentOnShapeChangeFinished by rememberUpdatedState(onShapeChangeFinished)
    val currentOnLiveResize by rememberUpdatedState(onLiveResize)
    val currentGridColumns by rememberUpdatedState(gridColumns)
    val currentHeightUnits by rememberUpdatedState(heightUnits)

    // Continuous resize during drag — no grid snap, follows finger smoothly.
    // Grid snap only happens on release (in onShapeChangeFinishedWrapped).
    val onShapeChange: (Float, Float) -> Unit = remember {
        { w: Float, h: Float ->
            val clampedW = DashboardUiConfig.clampWidthFraction(w)
            val clampedH = DashboardUiConfig.clampHeightScale(h)
            draftWidth = clampedW
            draftHeightScale = clampedH
            currentOnLiveResize(clampedW, clampedH)
        }
    }

    val onShapeChangeFinishedWrapped: (Float, Float) -> Unit = remember {
        { w: Float, h: Float ->
            val snappedWidth = widthForGridSpan(gridSpanForWidth(w, currentGridColumns), currentGridColumns)
            val snappedHeight = heightForGridUnits(h, currentHeightUnits)
            draftWidth = snappedWidth
            draftHeightScale = snappedHeight
            currentOnShapeChangeFinished(snappedWidth, snappedHeight)
        }
    }

    // Ghost: when this card is being dragged, fade it to ~30% opacity
    val ghostAlpha by animateFloatAsState(
        targetValue = if (isBeingDragged) 0.30f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "ghost_alpha",
    )

    Box(
        modifier = modifier
            .height(heightDp)
            .onSizeChanged { size -> containerWidthPx = size.width.toFloat().coerceAtLeast(1f) }
            .onGloballyPositioned { coords ->
                cardBounds[section] = coords.boundsInWindow()
            }
            .pointerInput(section) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongPress()
                    },
                )
            },
        contentAlignment = Alignment.TopStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = ghostAlpha }
                .clip(shape),
        ) {
            content()
        }

        // Resize handles — shown when card is in selected/resize mode (not during drag)
        if (selected && !isBeingDragged) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(BorderStroke(2.dp, MaterialTheme.colors.primary), shape),
            )
            DashboardResizeHandle(
                contentDescription = stringResource(R.string.dashboard_resize_left_a11y),
                horizontalDirection = -1f, verticalDirection = 0f,
                containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            DashboardResizeHandle(
                contentDescription = stringResource(R.string.dashboard_resize_right_a11y),
                horizontalDirection = 1f, verticalDirection = 0f,
                containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
            DashboardResizeHandle(
                contentDescription = stringResource(R.string.dashboard_resize_top_a11y),
                horizontalDirection = 0f, verticalDirection = -1f,
                containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            DashboardResizeHandle(
                contentDescription = stringResource(R.string.dashboard_resize_bottom_a11y),
                horizontalDirection = 0f, verticalDirection = 1f,
                containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
internal fun DashboardResizeHandle(
    contentDescription: String,
    horizontalDirection: Float,
    verticalDirection: Float,
    containerWidthPx: Float,
    baseHeightPx: Float,
    widthFraction: Float,
    heightScale: Float,
    onShapeChange: (Float, Float) -> Unit,
    onShapeChangeFinished: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestWidth by rememberUpdatedState(widthFraction)
    val latestHeightScale by rememberUpdatedState(heightScale)
    var dragStartWidth by remember { mutableFloatStateOf(widthFraction) }
    var dragStartHeightScale by remember { mutableFloatStateOf(heightScale) }
    var dragDistanceXPx by remember { mutableFloatStateOf(0f) }
    var dragDistanceYPx by remember { mutableFloatStateOf(0f) }
    var lastWidth by remember { mutableFloatStateOf(widthFraction) }
    var lastHeightScale by remember { mutableFloatStateOf(heightScale) }
    val isHorizontalHandle = horizontalDirection != 0f
    val handleColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
    val gripColor = MaterialTheme.colors.primary.copy(alpha = 0.82f)

    Box(
        modifier = modifier
            .width(if (isHorizontalHandle) 18.dp else 58.dp)
            .height(if (isHorizontalHandle) 58.dp else 18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(handleColor)
            .border(BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.45f)), RoundedCornerShape(9.dp))
            .pointerInput(horizontalDirection, verticalDirection, containerWidthPx, baseHeightPx) {
                detectDragGestures(
                    onDragStart = {
                        dragStartWidth = latestWidth; dragStartHeightScale = latestHeightScale
                        dragDistanceXPx = 0f; dragDistanceYPx = 0f
                        lastWidth = latestWidth; lastHeightScale = latestHeightScale
                    },
                    onDragEnd = { onShapeChangeFinished(lastWidth, lastHeightScale) },
                    onDragCancel = { onShapeChangeFinished(lastWidth, lastHeightScale) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragDistanceXPx += dragAmount.x
                        dragDistanceYPx += dragAmount.y
                        val nextWidth = DashboardUiConfig.clampWidthFraction(
                            dragStartWidth + horizontalDirection * dragDistanceXPx / containerWidthPx,
                        )
                        val nextHeight = DashboardUiConfig.clampHeightScale(
                            dragStartHeightScale + verticalDirection * dragDistanceYPx / baseHeightPx,
                        )
                        lastWidth = nextWidth; lastHeightScale = nextHeight
                        onShapeChange(nextWidth, nextHeight)
                    },
                )
            }
            .semantics { role = Role.Button; this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(if (isHorizontalHandle) 3.dp else 30.dp)
                .height(if (isHorizontalHandle) 30.dp else 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(gripColor),
        )
    }
}

internal fun gridSpanForWidth(widthFraction: Float, gridColumns: Int): Int =
    (DashboardUiConfig.clampWidthFraction(widthFraction) * gridColumns)
        .roundToInt().coerceIn(1, gridColumns)

internal fun widthForGridSpan(span: Int, gridColumns: Int): Float =
    (span.coerceIn(1, gridColumns).toFloat() / gridColumns)
        .coerceIn(DashboardUiConfig.MIN_CARD_WIDTH_FRACTION, DashboardUiConfig.MAX_CARD_WIDTH_FRACTION)

internal fun heightForGridUnits(heightScale: Float, heightUnits: Int): Float {
    val clamped = DashboardUiConfig.clampHeightScale(heightScale)
    val step = 1f / heightUnits.coerceAtLeast(1)
    return (clamped / step).roundToInt().coerceAtLeast(1).times(step)
        .coerceIn(DashboardUiConfig.MIN_CARD_HEIGHT_SCALE, DashboardUiConfig.MAX_CARD_HEIGHT_SCALE)
}
