package com.bruhascended.fitapp.util

import androidx.compose.runtime.Immutable
import java.util.*

@Immutable
data class BarGraphData(
    val height: Float = 0f,
    val x: String = "",
    val startTime: Date
)
