package com.bruhascended.fitapp.ui.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.repository.PreferencesRepository
import kotlin.math.min
import com.bruhascended.fitapp.ui.theme.Blue500
import com.bruhascended.fitapp.ui.theme.Green200
import com.bruhascended.fitapp.ui.theme.Red200

@Composable
fun ConcentricCircles(
    canvasSize: Dp,
    todayActivityData: DayEntry,
    todayNutrientData: com.bruhascended.db.food.entities.DayEntry,
    activityGoals: PreferencesRepository.ActivityPreferences,
    nutrientGoals: PreferencesRepository.NutritionPreferences
) {
    val animateList = mutableListOf(
        getTarget(nutrientGoals.calories, todayNutrientData.calories.toFloat()),
        getTarget(activityGoals.calories, todayActivityData.totalCalories),
        getTarget(activityGoals.steps, todayActivityData.totalSteps.toFloat()),
    )
    Column(
        modifier = Modifier
            // Square layout + square draw math: weighted Row/Surface can yield a wide bounds;
            // arcs were using width and height separately → elliptical rings.
            .requiredSize(canvasSize)
            .drawBehind {
                backgroundProgressCircle()
                foregroundProgressCircle(animateList)
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

fun DrawScope.foregroundProgressCircle(animateList: MutableList<Float>) {
    val side = min(size.width, size.height)
    val strokeWidth = side / 10f

    val d1 = side / 4f
    val d2 = side / 2f
    val d3 = side / 4f * 3f

    drawArc(
        Green200,
        -90f, animateList[0], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d1, d1),
        topLeft = Offset(
            x = (size.width - d1) / 2f,
            y = (size.height - d1) / 2f,
        ),
    )
    drawArc(
        Red200,
        -90f, animateList[1], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d2, d2),
        topLeft = Offset(
            x = (size.width - d2) / 2f,
            y = (size.height - d2) / 2f,
        ),
    )
    drawArc(
        Blue500,
        -90f, animateList[2], false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d3, d3),
        topLeft = Offset(
            x = (size.width - d3) / 2f,
            y = (size.height - d3) / 2f,
        ),
    )
}

fun DrawScope.backgroundProgressCircle() {
    val side = min(size.width, size.height)
    val strokeWidth = side / 9f
    val d1 = side / 4f
    val d2 = side / 2f
    val d3 = side / 4f * 3f

    drawArc(
        Green200.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d1, d1),
        topLeft = Offset(
            x = (size.width - d1) / 2f,
            y = (size.height - d1) / 2f,
        ),
    )
    drawArc(
        Red200.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d2, d2),
        topLeft = Offset(
            x = (size.width - d2) / 2f,
            y = (size.height - d2) / 2f,
        ),
    )
    drawArc(
        Blue500.copy(alpha = 0.2f),
        0f, 360f, false,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
        ), size = Size(d3, d3),
        topLeft = Offset(
            x = (size.width - d3) / 2f,
            y = (size.height - d3) / 2f,
        ),
    )
}
