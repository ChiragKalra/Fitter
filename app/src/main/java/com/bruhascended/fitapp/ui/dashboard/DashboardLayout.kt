package com.bruhascended.fitapp.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import com.bruhascended.fitapp.R

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

@Composable
internal fun DashboardWidgetCard(
    section: DashboardSection,
    selected: Boolean,
    committedWidthFraction: Float,
    committedHeightScale: Float,
    gridColumns: Int,
    heightUnits: Int,
    cardBounds: MutableMap<DashboardSection, Rect>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onReorder: (from: DashboardSection, to: DashboardSection) -> Unit,
    onReorderEnd: () -> Unit,
    onShapeChangeFinished: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var draftWidth by remember(committedWidthFraction) { mutableFloatStateOf(committedWidthFraction) }
    var draftHeightScale by remember(committedHeightScale) { mutableFloatStateOf(committedHeightScale) }

    val shape = remember { RoundedCornerShape(12.dp) }
    val baseHeight = 180.dp

    // Track container width in px via onSizeChanged instead of BoxWithConstraints
    var containerWidthPx by remember { mutableFloatStateOf(1f) }

    val density = LocalDensity.current
    val baseHeightPx = remember(density) { with(density) { baseHeight.toPx() }.coerceAtLeast(1f) }
    val targetHeight = baseHeight * DashboardUiConfig.clampHeightScale(draftHeightScale)
    val fullRowWidthPx = (containerWidthPx / committedWidthFraction.coerceAtLeast(0.01f))

    val onShapeChange: (Float, Float) -> Unit = remember {
        { w: Float, h: Float ->
            draftWidth = w
            draftHeightScale = h
        }
    }

    val currentOnShapeChangeFinished by rememberUpdatedState(onShapeChangeFinished)
    val currentGridColumns by rememberUpdatedState(gridColumns)
    val currentHeightUnits by rememberUpdatedState(heightUnits)

    val onShapeChangeFinishedWrapped: (Float, Float) -> Unit = remember {
        { w: Float, h: Float ->
            val snappedWidth = widthForGridSpan(gridSpanForWidth(w, currentGridColumns), currentGridColumns)
            val snappedHeight = heightForGridUnits(h, currentHeightUnits)
            draftWidth = snappedWidth
            draftHeightScale = snappedHeight
            currentOnShapeChangeFinished(snappedWidth, snappedHeight)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { size -> containerWidthPx = size.width.toFloat().coerceAtLeast(1f) }
            .onGloballyPositioned { coords ->
                cardBounds[section] = coords.boundsInWindow()
            },
        contentAlignment = Alignment.TopStart,
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(targetHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(targetHeight)
                    .clip(shape)
                    .pointerInput(section, selected) {
                        if (selected) {
                            detectDragGestures(
                                onDragEnd = { onReorderEnd() },
                                onDragCancel = { onReorderEnd() },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val bounds = cardBounds[section] ?: return@detectDragGestures
                                    val windowPos = androidx.compose.ui.geometry.Offset(
                                        bounds.left + change.position.x,
                                        bounds.top + change.position.y,
                                    )
                                    val target = cardBounds.entries
                                        .firstOrNull { (s, r) -> s != section && r.contains(windowPos) }
                                        ?.key
                                    if (target != null) onReorder(section, target)
                                },
                            )
                        } else {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onLongPress() },
                                onDragEnd = { onReorderEnd() },
                                onDragCancel = { onReorderEnd() },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val bounds = cardBounds[section] ?: return@detectDragGesturesAfterLongPress
                                    val windowPos = androidx.compose.ui.geometry.Offset(
                                        bounds.left + change.position.x,
                                        bounds.top + change.position.y,
                                    )
                                    val target = cardBounds.entries
                                        .firstOrNull { (s, r) -> s != section && r.contains(windowPos) }
                                        ?.key
                                    if (target != null) onReorder(section, target)
                                },
                            )
                        }
                    }
                    .pointerInput(section, selected) {
                        detectTapGestures(onTap = { onClick() })
                    },
            ) {
                content()
            }

            if (selected) {
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
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = 36.dp),
                )
                DashboardResizeHandle(
                    contentDescription = stringResource(R.string.dashboard_resize_right_a11y),
                    horizontalDirection = 1f, verticalDirection = 0f,
                    containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                    widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                    heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                    onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-36).dp),
                )
                DashboardResizeHandle(
                    contentDescription = stringResource(R.string.dashboard_resize_top_a11y),
                    horizontalDirection = 0f, verticalDirection = -1f,
                    containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                    widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                    heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                    onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-9).dp),
                )
                DashboardResizeHandle(
                    contentDescription = stringResource(R.string.dashboard_resize_bottom_a11y),
                    horizontalDirection = 0f, verticalDirection = 1f,
                    containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                    widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                    heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                    onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinishedWrapped,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 9.dp),
                )
            }
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
