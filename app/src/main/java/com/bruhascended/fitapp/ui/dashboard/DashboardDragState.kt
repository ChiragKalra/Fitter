package com.bruhascended.fitapp.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * Single source of truth for a drag-to-reorder gesture.
 *
 * Lifecycle:
 *   IDLE  →  LIFTING  →  DRAGGING  →  SETTLING  →  IDLE
 *
 * - IDLE:      no drag in progress.
 * - LIFTING:   long-press recognised; the card is scaling up / getting elevation.
 * - DRAGGING:  finger is moving; the overlay follows the finger, other items shift.
 * - SETTLING:  finger released; the overlay animates to its final slot, then clears.
 */
class DashboardDragState {

    enum class Phase { IDLE, LIFTING, DRAGGING, SETTLING }

    /** Current phase of the gesture. */
    var phase by mutableStateOf(Phase.IDLE)
        private set

    /** The section being dragged (null when IDLE). */
    var draggedSection by mutableStateOf<DashboardSection?>(null)
        private set

    /** Finger position in window coordinates — used to position the overlay. */
    var fingerWindowOffset by mutableStateOf(Offset.Zero)
        private set

    /** Finger position at the moment drag started — used to detect if finger actually moved. */
    var initialFingerWindowOffset by mutableStateOf(Offset.Zero)
        private set

    /** The offset of the touch point relative to the card's top-left when the drag started. */
    var touchOffsetInCard by mutableStateOf(Offset.Zero)
        private set

    /** True when the dragged card is hovering over the "Remove" drop zone at the top. */
    var hoveringOverRemoveZone by mutableStateOf(false)

    /** Accumulated drag delta from the start position — used for auto-scroll edge detection. */
    var dragDeltaY by mutableFloatStateOf(0f)
        private set

    /** The window-coordinate bounds of the card when the drag started. */
    var initialCardTop by mutableFloatStateOf(0f)
        private set

    /** Original card dimensions in px — captured before content scales down. */
    var initialCardWidthPx by mutableFloatStateOf(0f)
        private set
    var initialCardHeightPx by mutableFloatStateOf(0f)
        private set

    val isDragging: Boolean get() = phase == Phase.DRAGGING || phase == Phase.LIFTING

    /** True once the finger has moved far enough from the initial position to count as a drag. */
    val hasMoved: Boolean get() {
        val dx = fingerWindowOffset.x - initialFingerWindowOffset.x
        val dy = fingerWindowOffset.y - initialFingerWindowOffset.y
        return dx * dx + dy * dy > MOVE_THRESHOLD_PX_SQ
    }

    // ── mutations ──────────────────────────────────────────────────────

    fun startLift(
        section: DashboardSection,
        fingerWindow: Offset,
        cardTopInWindow: Float,
        touchInCard: Offset,
        cardWidthPx: Float,
        cardHeightPx: Float,
    ) {
        draggedSection = section
        fingerWindowOffset = fingerWindow
        initialFingerWindowOffset = fingerWindow
        initialCardTop = cardTopInWindow
        touchOffsetInCard = touchInCard
        initialCardWidthPx = cardWidthPx
        initialCardHeightPx = cardHeightPx
        dragDeltaY = 0f
        hoveringOverRemoveZone = false
        phase = Phase.LIFTING
    }

    fun beginDrag() {
        if (phase == Phase.LIFTING) phase = Phase.DRAGGING
    }

    fun updateDrag(fingerWindow: Offset, deltaY: Float) {
        fingerWindowOffset = fingerWindow
        dragDeltaY += deltaY
    }

    fun startSettling() {
        phase = Phase.SETTLING
    }

    fun reset() {
        phase = Phase.IDLE
        draggedSection = null
        fingerWindowOffset = Offset.Zero
        initialFingerWindowOffset = Offset.Zero
        touchOffsetInCard = Offset.Zero
        initialCardWidthPx = 0f
        initialCardHeightPx = 0f
        dragDeltaY = 0f
        hoveringOverRemoveZone = false
    }

    companion object {
        /** ~15px movement threshold before we consider it a real drag vs. long-press-in-place. */
        private const val MOVE_THRESHOLD_PX_SQ = 15f * 15f
    }
}
