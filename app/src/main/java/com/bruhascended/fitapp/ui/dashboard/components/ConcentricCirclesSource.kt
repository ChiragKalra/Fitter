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
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.repository.PreferencesRepository

@Composable
fun ConcentricCircles(
    canvasSize: Dp = 300.dp,
    dayData: DayEntry,
    activityGoals: PreferencesRepository.ActivityPreferences,
    nutrientGoals: PreferencesRepository.NutritionPreferences
) {
    val strokeWidth = canvasSize.value / 5f
    val sSize = Size(300.dp.value, 300.dp.value)
    val animSpeed = 1200
    val sweepCalories by animateFloatAsState(
        targetValue = getTarget(activityGoals.calories, dayData.totalCalories),
        animationSpec = tween(animSpeed)
    )
    val sweepSteps by animateFloatAsState(
        targetValue = getTarget(activityGoals.steps, dayData.totalSteps.toFloat()),
        animationSpec = tween(animSpeed)
    )
    val sweepDistance by animateFloatAsState(
        targetValue = getTarget(activityGoals.distance, dayData.totalDistance.toFloat()),
        animationSpec = tween(animSpeed)
    )
    val sweepDuration by animateFloatAsState(
        targetValue = getTarget(activityGoals.duration, dayData.totalDuration.toFloat()),
        animationSpec = tween(animSpeed)
    )
    val sweepConsumed by animateFloatAsState(
        targetValue = getTarget(nutrientGoals.calories, 1500f),
        animationSpec = tween(animSpeed)
    )
    val animateList = mutableListOf(
        sweepDistance,
        sweepDuration,
        sweepConsumed,
        sweepCalories,
        sweepSteps // todo order to be determined by user
    )
    Column(
        modifier = Modifier
            .size(canvasSize)
            .padding(4.dp)
            .drawBehind {
                backgroundProgressCircle(strokeWidth, sSize)
                foregroundProgressCircle(strokeWidth, sSize, animateList)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
    }
}

fun getTarget(goal: Long, curr: Float): Float {
    val perc = curr / goal
    return if (perc <= 1) perc * 360f
    else 360f
}

fun DrawScope.foregroundProgressCircle(
    strokeWidth: Float,
    sSize: Size,
    animateList: MutableList<Float>
) {
    drawArc(
        Color.Blue,
        -90f, animateList[0], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f),
        topLeft = Offset(
            x = (size.width - (size.width / 5f)) / 2f,
            y = (size.height - (size.height / 5f)) / 2f,
        )
    )
    drawArc(
        Color.Magenta,
        -90f, animateList[1], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 1.8f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 1.8f) / 2f,
            y = (size.height - (size.height / 5f) * 1.8f) / 2f,
        )
    )
    drawArc(
        Color.Green,
        -90f, animateList[2], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 2.6f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 2.6f) / 2f,
            y = (size.height - (size.height / 5f) * 2.6f) / 2f,
        )
    )
    drawArc(
        Color.Blue,
        -90f, animateList[3], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 3.4f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 3.4f) / 2f,
            y = (size.height - (size.height / 5f) * 3.4f) / 2f,
        )
    )
    drawArc(
        Color.Green,
        -90f, animateList[4], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 4.2f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 4.2f) / 2f,
            y = (size.height - (size.height / 5f) * 4.2f) / 2f,
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
        ), size = (size / 5f),
        topLeft = Offset(
            x = (size.width - (size.width / 5f)) / 2f,
            y = (size.height - (size.height / 5f)) / 2f,
        )
    )
    drawArc(
        Color.Magenta.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 1.8f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 1.8f) / 2f,
            y = (size.height - (size.height / 5f) * 1.8f) / 2f,
        )
    )
    drawArc(
        Color.Green.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 2.6f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 2.6f) / 2f,
            y = (size.height - (size.height / 5f) * 2.6f) / 2f,
        )
    )
    drawArc(
        Color.Blue.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 3.4f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 3.4f) / 2f,
            y = (size.height - (size.height / 5f) * 3.4f) / 2f,
        )
    )
    drawArc(
        Color.Green.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 5f) * 4.2f,
        topLeft = Offset(
            x = (size.width - (size.width / 5f) * 4.2f) / 2f,
            y = (size.height - (size.height / 5f) * 4.2f) / 2f,
        )
    )
}
