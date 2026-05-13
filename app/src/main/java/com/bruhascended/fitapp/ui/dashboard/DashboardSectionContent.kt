package com.bruhascended.fitapp.ui.dashboard

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.NutrientCard
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.theme.Blue100
import com.bruhascended.fitapp.ui.theme.Blue500
import com.bruhascended.fitapp.ui.theme.Green200
import com.bruhascended.fitapp.ui.theme.Purple200
import com.bruhascended.fitapp.ui.theme.Red200
import com.bruhascended.fitapp.ui.theme.Yellow500
import com.bruhascended.fitapp.util.BarGraphData
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.floor

internal const val DASHBOARD_GREETING_KEY = "dashboard_greeting"
private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L

internal fun baseCardHeight(section: DashboardSection): Dp =
    when (section) {
        DashboardSection.SUMMARY_RING -> 220.dp
        DashboardSection.TODAY_STATS -> 142.dp
        DashboardSection.NUTRIENTS -> 150.dp
        else -> 180.dp
    }

internal fun dashboardMetricFor(section: DashboardSection): DashboardTrendMetric? =
    when (section) {
        DashboardSection.ACTIVE_ENERGY -> DashboardTrendMetric.ACTIVE_ENERGY
        DashboardSection.CALORIE_BALANCE -> DashboardTrendMetric.CALORIE_BALANCE
        DashboardSection.WEIGHT_DELTA -> DashboardTrendMetric.NET_WEIGHT
        DashboardSection.WEIGHT_PROJECTION -> DashboardTrendMetric.WEIGHT_PROJECTION
        else -> null
    }

@Composable
internal fun DashboardGreetingCard(
    latestFoodLoggedAt: Long?,
    latestWeightLoggedAt: Long?,
    modifier: Modifier = Modifier,
) {
    val greeting = remember { dashboardGreeting() }
    val tip = remember(latestFoodLoggedAt, latestWeightLoggedAt) {
        dashboardConsistencyTip(daysSince(latestFoodLoggedAt), daysSince(latestWeightLoggedAt))
    }
    Column(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colors.surface)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = greeting, color = MaterialTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 30.sp, maxLines = 1)
        Text(text = tip, color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f), fontSize = 14.sp, lineHeight = 18.sp, maxLines = 2, modifier = Modifier.padding(top = 6.dp))
    }
}

private fun dashboardGreeting(now: Calendar = Calendar.getInstance(TimeZone.getDefault())): String =
    when (now.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "What's up"
    }

private fun dashboardConsistencyTip(foodDays: Int?, weightDays: Int?): String =
    when {
        foodDays == null && weightDays == null -> "Log a meal and your weight today to start a clean baseline."
        foodDays == null -> "No food logged yet. Start with one meal today."
        weightDays == null -> "No weight logged yet. Add a quick weigh-in to start the trend."
        foodDays == 0 && weightDays == 0 -> "Food and weight are current today. Keep the streak clean."
        foodDays > 0 && weightDays > 0 -> "You haven't logged food in ${dayWord(foodDays)} or weight in ${dayWord(weightDays)}. Let's keep up the consistency."
        foodDays > 0 -> "You haven't logged food in ${dayWord(foodDays)}. A quick meal keeps the trend honest."
        else -> "You haven't logged weight in ${dayWord(weightDays)}. A quick weigh-in keeps the trend honest."
    }

private fun daysSince(timeMillis: Long?, nowMillis: Long = System.currentTimeMillis()): Int? {
    if (timeMillis == null) return null
    return ((dayStartMillis(nowMillis) - dayStartMillis(timeMillis)) / ONE_DAY_MILLIS).coerceAtLeast(0L).toInt()
}

private fun dayStartMillis(timeMillis: Long): Long =
    Calendar.getInstance(TimeZone.getDefault()).run {
        this.timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        this.timeInMillis
    }

private fun dayWord(days: Int?): String = if ((days ?: 0) == 1) "1 day" else "${days ?: 0} days"

@Composable
internal fun SectionContent(
    section: DashboardSection,
    outerCircleDiameter: Dp,
    todayActivityData: DayEntry,
    todayNutrientData: com.bruhascended.db.food.entities.DayEntry,
    activityGoals: PreferencesRepository.ActivityPreferences,
    nutrientGoals: PreferencesRepository.NutritionPreferences,
    stepsList: List<BarGraphData>,
    energyExpList: List<BarGraphData>,
    activeEnergyWeekList: List<BarGraphData>,
    calorieBalanceWeekList: List<BarGraphData>,
    weightDeltaWeekList: List<BarGraphData>,
    projectedWeightWeekList: List<BarGraphData>,
    proteinWeekList: List<BarGraphData>,
    carbsWeekList: List<BarGraphData>,
    fatWeekList: List<BarGraphData>,
    sugarWeekList: List<BarGraphData>,
    context: Context,
) {
    when (section) {
        DashboardSection.SUMMARY_RING -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ConcentricCircles(outerCircleDiameter, todayActivityData, todayNutrientData, activityGoals, nutrientGoals)
        }
        DashboardSection.TODAY_STATS -> CurrentDayStats(todayActivityData, todayNutrientData, Modifier.fillMaxSize())
        DashboardSection.STEPS_WEEK -> OverViewCard(stepsList, context, "Steps", "steps", activityGoals.steps, Blue500, modifier = Modifier.fillMaxSize())
        DashboardSection.ENERGY_WEEK -> OverViewCard(energyExpList, context, "Total expenditure", "Cal", activityGoals.calories, Red200, modifier = Modifier.fillMaxSize())
        DashboardSection.ACTIVE_ENERGY -> TappableOverviewCard(activeEnergyWeekList, context, "Active energy", "kcal", activityGoals.calories, Color(0xFFFFA24A))
        DashboardSection.CALORIE_BALANCE -> TappableOverviewCard(calorieBalanceWeekList, context, "Calories in - TDEE", "kcal", maxOf(activityGoals.calories, nutrientGoals.calories), Color(0xFF2DD4BF))
        DashboardSection.WEIGHT_DELTA -> TappableOverviewCard(weightDeltaWeekList, context, "Net weight change", "kg", 1L, Color(0xFFA3E635))
        DashboardSection.WEIGHT_PROJECTION -> TappableOverviewCard(projectedWeightWeekList, context, "Projected weight change", "kg", 1L, Color(0xFFE879F9))
        DashboardSection.NUTRIENTS -> NutrientCard(todayNutrientData, modifier = Modifier.fillMaxSize())
        DashboardSection.NUTRIENT_PROTEIN -> OverViewCard(proteinWeekList, context, context.getString(DashboardSection.NUTRIENT_PROTEIN.titleRes), "g", nutrientGoals.proteins, Purple200, modifier = Modifier.fillMaxSize())
        DashboardSection.NUTRIENT_CARBS -> OverViewCard(carbsWeekList, context, context.getString(DashboardSection.NUTRIENT_CARBS.titleRes), "g", nutrientGoals.carbs, Yellow500, modifier = Modifier.fillMaxSize())
        DashboardSection.NUTRIENT_FAT -> OverViewCard(fatWeekList, context, context.getString(DashboardSection.NUTRIENT_FAT.titleRes), "g", nutrientGoals.fats, Blue100, modifier = Modifier.fillMaxSize())
        DashboardSection.NUTRIENT_ADDED_SUGAR -> OverViewCard(sugarWeekList, context, context.getString(DashboardSection.NUTRIENT_ADDED_SUGAR.titleRes), "g", nutrientGoals.addedSugar, Color(0xFF38BDF8), modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun TappableOverviewCard(data: List<BarGraphData>, context: Context, title: String, unit: String, goal: Long, color: Color) {
    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp))) {
        OverViewCard(data = data, context = context, s = title, unit = unit, repo = goal, color = color, modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CurrentDayStats(todayData: DayEntry, todayNutrientData: com.bruhascended.db.food.entities.DayEntry, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            CurrentDayItem("Cal", stringFormatter(todayData.totalCalories.toInt()), painterResource(id = R.drawable.ic_energy_burn), Red200, "Energy Burned")
            CurrentDayItem("", stringFormatter(todayData.totalSteps), painterResource(id = R.drawable.ic_steps), Blue500, "Steps")
            CurrentDayItem("Cal", stringFormatter(todayNutrientData.calories), painterResource(id = R.drawable.ic_consumed), Green200, "Energy Consumed")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            CurrentDayItem("km", stringFormatter(todayData.totalDistance.toFloat()), painterResource(id = R.drawable.ic_distance), MaterialTheme.colors.onSurface, "Distance")
            CurrentDayItem("min", stringFormatter(todayData.totalDuration / 60000f), painterResource(id = R.drawable.ic_duration), MaterialTheme.colors.onSurface, "Duration")
        }
    }
}

private fun stringFormatter(data: Any): String {
    val str = String.format("%.1f", data.toString().toFloat())
    val num = str.toFloat()
    return if (ceil(num) == floor(num)) num.toInt().toString() else str
}

@Composable
internal fun CurrentDayItem(unit: String = "", data: String, painter: Painter, color: Color, description: String) {
    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(painter = painter, tint = color, contentDescription = description, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
        Text(text = "$data $unit", color = MaterialTheme.colors.onSurface, fontWeight = FontWeight.SemiBold)
    }
}
