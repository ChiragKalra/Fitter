package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ConcentricCircles(canvasSize: Dp = 300.dp, indicatorValue: Float = 0f) {
    val strokeWidth = canvasSize.value / 4f
    val sSize = Size(300.dp.value, 300.dp.value)

    val sweepAngle by animateFloatAsState(
        targetValue = indicatorValue,
        animationSpec = tween(1000)
    )

    Column(
        modifier = Modifier
            .size(canvasSize)
            .padding(4.dp)
            .drawBehind {
                backgroundProgressCircle(strokeWidth, sSize)
                foregroundProgressCircle(strokeWidth, sSize, sweepAngle)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
    }
}

fun DrawScope.foregroundProgressCircle(strokeWidth: Float, sSize: Size, sweepAngle: Float) {
    drawArc(
        Color.Blue,
        -90f, sweepAngle, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f),
        topLeft = Offset(
            x = (size.width - (size.width / 3f)) / 2f,
            y = (size.height - (size.height / 3f)) / 2f,
        )
    )
    drawArc(
        Color.Magenta,
        -90f, sweepAngle, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f) * 1.6f,
        topLeft = Offset(
            x = (size.width - (size.width / 3f) * 1.6f) / 2f,
            y = (size.height - (size.height / 3f) * 1.6f) / 2f,
        )
    )
    drawArc(
        Color.Green,
        -90f, sweepAngle, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f) * 2.2f,
        topLeft = Offset(
            x = (size.width - (size.width / 3f) * 2.2f) / 2f,
            y = (size.height - (size.height / 3f) * 2.2f) / 2f,
        )
    )
}

fun DrawScope.backgroundProgressCircle(strokeWidth: Float, sSize: Size) {
    drawArc(
        Color.Blue.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f),
        topLeft = Offset(
            x = (size.width - (size.width / 3f)) / 2f,
            y = (size.height - (size.height / 3f)) / 2f,
        )
    )
    drawArc(
        Color.Magenta.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f) * 1.6f,
        topLeft = Offset(
            x = (size.width - (size.width / 3f) * 1.6f) / 2f,
            y = (size.height - (size.height / 3f) * 1.6f) / 2f,
        )
    )
    drawArc(
        Color.Green.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 3f) * 2.2f,
        topLeft = Offset(
            x = (size.width - (size.width / 3f) * 2.2f) / 2f,
            y = (size.height - (size.height / 3f) * 2.2f) / 2f,
        )
    )
}
