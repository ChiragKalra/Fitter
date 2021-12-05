package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val canvasSize = 300.dp
private val radius = canvasSize.value / 2

@Composable
// TODO : pass canvas size as param in animated circle
fun AnimatedCircle(indicatorValue: Float = 150f) {
    val animatedIndicatorValue = remember {
        Animatable(initialValue = 0f)
    }
    LaunchedEffect(key1 = indicatorValue) {
        animatedIndicatorValue.animateTo(indicatorValue)
    }

    val sweepAngle by animateFloatAsState(
        targetValue = animatedIndicatorValue.value,
        animationSpec = tween(1000)
    )

    Column(modifier = Modifier
        .size(canvasSize)
        .drawBehind {
            backgroundIndicator(size / 1.25f)
            foregroundIndicator(size / 1.25f, sweepAngle)
        }) {
    }
}

fun DrawScope.backgroundIndicator(ComponentSize: Size) {
    drawArc(
        Color.Gray.copy(alpha = 0.3f),
        0f, 360f, false,
        style = Stroke(
            width = 100f,
            cap = StrokeCap.Round
        ), size = ComponentSize,
        topLeft = Offset(
            x = (size.width - ComponentSize.width) / 2f,
            y = (size.height - ComponentSize.height) / 2f,
        )
    )
}

fun DrawScope.foregroundIndicator(ComponentSize: Size, sweepAngle: Float) {
    drawArc(
        Color.Magenta,
        -90f, sweepAngle = sweepAngle, false,
        style = Stroke(
            width = 100f,
            cap = StrokeCap.Round
        ), size = ComponentSize,
        topLeft = Offset(
            x = (size.width - ComponentSize.width) / 2f,
            y = (size.height - ComponentSize.height) / 2f,
        )
    )
}


@Composable
@Preview(showBackground = true)
fun AnimatedCirclePreview() {
    AnimatedCircle()
}