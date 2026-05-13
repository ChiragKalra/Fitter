package com.bruhascended.fitapp.ui.dashboard.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.fitapp.util.BarGraphData
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun BarGraph(data: List<BarGraphData>, context: Context, unit: String?, goal: Long, color: Color) {
    if (data.isEmpty()) return
    val minValue = min(0f, data.minOfOrNull { it.height } ?: 0f)
    val maxValue = max(max(1f, data.maxOfOrNull { it.height } ?: 0f), goal.toFloat())
    val span = (maxValue - minValue).takeIf { it > 0.001f } ?: 1f
    val zeroLineColor = MaterialTheme.colors.onSurface.copy(alpha = 0.18f)
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
    ) {
        fun yFor(value: Float): Float =
            size.height - ((value - minValue) / span) * size.height

        val zeroY = yFor(0f)
        val goalY = yFor(goal.toFloat())
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = zeroLineColor,
            strokeWidth = 1f,
            start = Offset(0f, zeroY),
            end = Offset(size.width, zeroY),
        )
        drawLine(
            color = color,
            strokeWidth = 1f,
            start = Offset(0f, goalY),
            end = Offset(size.width, goalY),
            pathEffect = pathEffect,
        )

        val slotWidth = size.width / data.size
        val barWidth = (slotWidth * 0.34f).coerceAtLeast(3.dp.toPx())
        data.forEachIndexed { index, item ->
            val centerX = slotWidth * index + slotWidth / 2f
            val valueY = yFor(item.height)
            drawRect(
                color = color,
                topLeft = Offset(centerX - barWidth / 2f, min(zeroY, valueY)),
                size = Size(barWidth, abs(zeroY - valueY)),
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        data.forEach { item ->
            Text(
                text = item.x,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
