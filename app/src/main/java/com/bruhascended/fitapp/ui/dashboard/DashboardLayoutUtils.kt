package com.bruhascended.fitapp.ui.dashboard

/**
 * Applies a reorder of visible dashboard sections onto the underlying full [order] list using the
 * same index semantics as [MutableList.add] + [MutableList.removeAt] in one expression — i.e.
 * [toVisible] refers to indices in the visible list BEFORE the move.
 */
internal fun reorderVisibleInFullOrder(
    order: List<DashboardSection>,
    hiddenIds: Set<DashboardSection>,
    fromVisible: Int,
    toVisible: Int,
): List<DashboardSection> {
    if (fromVisible == toVisible || order.isEmpty()) return order
    val globals = order.mapIndexedNotNull { idx, sec ->
        if (sec !in hiddenIds) idx else null
    }
    if (fromVisible !in globals.indices || toVisible !in globals.indices) return order

    val m = order.toMutableList()
    val gvFrom = globals[fromVisible]
    val gvTo = globals[toVisible]
    m.add(gvTo, m.removeAt(gvFrom))
    return m
}
