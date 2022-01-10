package com.bruhascended.fitapp.ui.dashboard.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.theme.*

@Composable
fun ConcentricCircles(
    canvasSize: Dp,
    todayActivityData: DayEntry,
    todayNutrientData: com.bruhascended.db.food.entities.DayEntry,
    activityGoals: PreferencesRepository.ActivityPreferences,
    nutrientGoals: PreferencesRepository.NutritionPreferences
) {
    val strokeWidth = canvasSize.value / 3.2f
    val sSize = Size(300.dp.value, 300.dp.value)
    val animSpeed = 1200
    val sweepCalories by animateFloatAsState(
        targetValue = getTarget(activityGoals.calories, todayActivityData.totalCalories),
        animationSpec = tween(animSpeed)
    )
    val sweepSteps by animateFloatAsState(
        targetValue = getTarget(activityGoals.steps, todayActivityData.totalSteps.toFloat()),
        animationSpec = tween(animSpeed)
    )
    val sweepConsumed by animateFloatAsState(
        targetValue = getTarget(nutrientGoals.calories, todayNutrientData.calories.toFloat()),
        animationSpec = tween(animSpeed)
    )
    val animateList = mutableListOf(
        sweepConsumed,
        sweepCalories,
        sweepSteps
    )
    Column(
        modifier = Modifier
            .size(canvasSize)
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
    Log.d("dbg_small","$strokeWidth $size")
    drawArc(
        Green200,
        -90f, animateList[0], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f),
        topLeft = Offset(
            x = (size.width - (size.width / 4f)) / 2f,
            y = (size.height - (size.height / 4f)) / 2f,
        )
    )
    drawArc(
        Red200,
        -90f, animateList[1], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f) * 2f,
        topLeft = Offset(
            x = (size.width - (size.width / 4f) * 2f) / 2f,
            y = (size.height - (size.height / 4f) * 2f) / 2f,
        )
    )
    drawArc(
        Blue500,
        -90f, animateList[2], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f) * 3f,
        topLeft = Offset(
            x = (size.width - (size.width / 4f) * 3f) / 2f,
            y = (size.height - (size.height / 4f) * 3f) / 2f,
        )
    )
}

fun DrawScope.backgroundProgressCircle(strokeWidth: Float, sSize: Size) {
    drawArc(
        Green200.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f),
        topLeft = Offset(
            x = (size.width - (size.width / 4f)) / 2f,
            y = (size.height - (size.height / 4f)) / 2f,
        )
    )
    drawArc(
        Red200.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f) * 2f,
        topLeft = Offset(
            x = (size.width - (size.width / 4f) * 2f) / 2f,
            y = (size.height - (size.height / 4f) * 2f) / 2f,
        )
    )
    drawArc(
        Blue500.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        ), size = (size / 4f) * 3f,
        topLeft = Offset(
            x = (size.width - (size.width / 4f) * 3f) / 2f,
            y = (size.height - (size.height / 4f) * 3f) / 2f,
        )
    )
}

@Composable
@Preview(showBackground = true)
fun pp(){
    val strokeWidth = 300.dp.value / 4f
    val sSize = Size(300.dp.value, 300.dp.value)
    Column(
        modifier = Modifier
            .size(300.dp)
            .drawBehind {
                backgroundProgressCircle(strokeWidth, sSize)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
    }
}