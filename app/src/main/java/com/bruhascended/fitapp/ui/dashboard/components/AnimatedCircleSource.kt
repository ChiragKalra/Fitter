package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// TODO : include color for background as well as foreground as param
@Composable
fun AnimatedCircle(indicatorValue: Float = 150f, canvasSize: Dp = 300.dp) {
    val strokeWidth = canvasSize.value / 3f

    val sweepAngle by animateFloatAsState(
        targetValue = indicatorValue,
        animationSpec = tween(1000)
    )

    Column(modifier = Modifier
        .size(canvasSize)
        .drawBehind {
            backgroundIndicator(size / 1.25f, strokeWidth)
            foregroundIndicator(size / 1.25f, strokeWidth, sweepAngle)
        }) {
    }
}

fun DrawScope.backgroundIndicator(
    circleSize: Size,
    strokeWidth: Float
) {
    drawArc(
        Color.Magenta.copy(alpha = 0.1f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = circleSize,
        topLeft = Offset(
            x = (size.width - circleSize.width) / 2f,
            y = (size.height - circleSize.height) / 2f,
        )
    )
}

fun DrawScope.foregroundIndicator(circleSize: Size, strokeWidth: Float, sweepAngle: Float) {
    drawArc(
        Color.Magenta,
        -90f, sweepAngle = sweepAngle, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = circleSize,
        topLeft = Offset(
            x = (size.width - circleSize.width) / 2f,
            y = (size.height - circleSize.height) / 2f,
        )
    )
}


@Composable
@Preview(showBackground = true)
fun AnimatedCirclePreview() {
    //AnimatedCircle()
}