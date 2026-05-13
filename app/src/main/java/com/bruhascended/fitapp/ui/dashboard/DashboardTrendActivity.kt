package com.bruhascended.fitapp.ui.dashboard

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.DayEntry as ActivityDayEntry
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.repository.WeightEntryRepository
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class DashboardTrendActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val metric = DashboardTrendMetric.fromId(intent.getStringExtra(EXTRA_METRIC))
        val loader = DashboardTrendLoader(application)
        setContent {
            FitAppTheme {
                TrendScreen(
                    metric = metric,
                    loader = loader,
                    onBack = { finish() },
                )
            }
        }
    }

    companion object {
        private const val EXTRA_METRIC = "extra_dashboard_trend_metric"

        fun intent(context: Context, metric: DashboardTrendMetric): Intent =
            Intent(context, DashboardTrendActivity::class.java)
                .putExtra(EXTRA_METRIC, metric.id)
    }
}

private enum class TrendRange(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YTD("YTD"),
    YEAR("Year"),
    TWO_YEARS("2Y"),
    THREE_YEARS("3Y"),
    FIVE_YEARS("5Y");
}

private data class TrendPoint(
    val time: Long,
    val value: Float,
)

private data class TrendUiModel(
    val metric: DashboardTrendMetric,
    val range: TrendRange,
    val primary: List<TrendPoint>,
    val smooth: List<TrendPoint>,
    val secondary: List<TrendPoint> = emptyList(),
    val summary: List<Pair<String, String>>,
    val note: String,
)

@Composable
private fun TrendScreen(
    metric: DashboardTrendMetric,
    loader: DashboardTrendLoader,
    onBack: () -> Unit,
) {
    var range by remember { mutableStateOf(TrendRange.MONTH) }
    var expanded by remember { mutableStateOf(false) }
    var model by remember { mutableStateOf<TrendUiModel?>(null) }

    LaunchedEffect(metric, range) {
        model = withContext(Dispatchers.IO) { loader.load(metric, range) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colors.background),
    ) {
        TopAppBar(
            title = { Text(metric.title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface,
            elevation = 0.dp,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 28.dp,
            ),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = model?.note ?: "Loading trend",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f),
                            fontSize = 13.sp,
                        )
                        Text(
                            text = "Smoothed trend + raw daily data",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.55f),
                            fontSize = 12.sp,
                        )
                    }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) {
                            Text(range.label)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            TrendRange.entries.forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        range = option
                                        expanded = false
                                    },
                                ) {
                                    Text(option.label)
                                }
                            }
                        }
                    }
                }
            }

            model?.let { trend ->
                item {
                    TrendChartCard(trend)
                }
                item {
                    SummaryGrid(trend.summary)
                }
            } ?: item {
                Text(
                    text = "Loading...",
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun TrendChartCard(model: TrendUiModel) {
    Card(
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = model.metric.title,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Legend(model)
            }
            Spacer(Modifier.height(14.dp))
            TrendChart(model)
        }
    }
}

@Composable
private fun Legend(model: TrendUiModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LegendDot(Color(0xFF38BDF8))
        Text("Trend", fontSize = 11.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f))
        if (model.secondary.isNotEmpty()) {
            Spacer(Modifier.width(10.dp))
            LegendDot(Color(0xFFF97316))
            Text("Logged", fontSize = 11.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .size(8.dp)
            .background(color, RoundedCornerShape(8.dp)),
    )
}

@Composable
private fun TrendChart(model: TrendUiModel) {
    val onSurface = MaterialTheme.colors.onSurface
    val grid = onSurface.copy(alpha = 0.14f)
    val allValues = (model.primary + model.smooth + model.secondary).map { it.value }
    val minValue = (allValues.minOrNull() ?: 0f).let { min(it, 0f) }
    val maxValue = (allValues.maxOrNull() ?: 0f).let { max(it, 0f) }
    val span = (maxValue - minValue).takeIf { it > 0.0001f } ?: 1f
    val paddedMin = minValue - span * 0.12f
    val paddedMax = maxValue + span * 0.12f
    val paddedSpan = paddedMax - paddedMin

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .border(1.dp, grid, RoundedCornerShape(10.dp))
            .padding(10.dp),
    ) {
        val chartLeft = 12.dp.toPx()
        val chartRight = size.width - 12.dp.toPx()
        val chartTop = 14.dp.toPx()
        val chartBottom = size.height - 18.dp.toPx()
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        fun x(index: Int, count: Int): Float =
            if (count <= 1) chartLeft else chartLeft + chartWidth * index / (count - 1)

        fun y(value: Float): Float =
            chartBottom - ((value - paddedMin) / paddedSpan) * chartHeight

        repeat(5) { i ->
            val yy = chartTop + chartHeight * i / 4f
            drawLine(grid, Offset(chartLeft, yy), Offset(chartRight, yy), strokeWidth = 1f)
        }
        if (0f in paddedMin..paddedMax) {
            val zero = y(0f)
            drawLine(onSurface.copy(alpha = 0.28f), Offset(chartLeft, zero), Offset(chartRight, zero), strokeWidth = 1.5f)
        }

        if (model.metric == DashboardTrendMetric.ACTIVE_ENERGY ||
            model.metric == DashboardTrendMetric.CALORIE_BALANCE
        ) {
            val count = model.primary.size
            val barWidth = (chartWidth / max(count, 1)).coerceIn(2f, 12.dp.toPx())
            model.primary.forEachIndexed { index, point ->
                val base = y(0f)
                val top = y(point.value)
                val left = x(index, count) - barWidth / 2f
                drawRect(
                    color = Color(0xFF38BDF8).copy(alpha = 0.34f),
                    topLeft = Offset(left, min(base, top)),
                    size = androidx.compose.ui.geometry.Size(barWidth, abs(base - top)),
                )
            }
        }

        drawSeries(
            points = model.primary,
            color = onSurface.copy(alpha = 0.24f),
            strokeWidth = 1.2f,
            x = ::x,
            y = ::y,
        )
        drawSeries(
            points = model.smooth,
            color = Color(0xFF38BDF8),
            strokeWidth = 3.2f,
            x = ::x,
            y = ::y,
        )
        if (model.secondary.isNotEmpty()) {
            drawSeries(
                points = model.secondary,
                color = Color(0xFFF97316),
                strokeWidth = 2.2f,
                x = ::x,
                y = ::y,
            )
            model.secondary.forEachIndexed { index, point ->
                drawCircle(
                    color = Color(0xFFF97316),
                    radius = 4.dp.toPx(),
                    center = Offset(x(index, model.secondary.size), y(point.value)),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSeries(
    points: List<TrendPoint>,
    color: Color,
    strokeWidth: Float,
    x: (Int, Int) -> Float,
    y: (Float) -> Float,
) {
    if (points.isEmpty()) return
    val path = Path()
    points.forEachIndexed { index, point ->
        val xx = x(index, points.size)
        val yy = y(point.value)
        if (index == 0) path.moveTo(xx, yy) else path.lineTo(xx, yy)
    }
    drawPath(path, color, style = Stroke(width = strokeWidth))
}

@Composable
private fun SummaryGrid(summary: List<Pair<String, String>>) {
    Card(
        backgroundColor = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(12.dp),
        elevation = 3.dp,
    ) {
        Column(Modifier.padding(16.dp)) {
            summary.chunked(2).forEachIndexed { index, row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { item ->
                        SummaryCell(item.first, item.second, Modifier.weight(1f))
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
                if (index != summary.chunked(2).lastIndex) {
                    Divider(Modifier.padding(vertical = 10.dp), color = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun SummaryCell(label: String, value: String, modifier: Modifier) {
    Column(modifier.padding(end = 12.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.55f))
        Text(
            value,
            color = MaterialTheme.colors.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
    }
}

private class DashboardTrendLoader(
    private val app: Application,
) {
    private val activityRepo = ActivityEntryRepository(app)
    private val foodRepo = FoodEntryRepository(app)
    private val weightRepo = WeightEntryRepository(app)

    fun load(metric: DashboardTrendMetric, range: TrendRange): TrendUiModel {
        val end = endOfToday()
        val start = startFor(range, end)
        val activityDays = activityRepo.loadRangeDayEntriesSync(start, end)
        val activityEntries = activityRepo.loadActivityEntriesRangeSync(start, end)
        val foodDays = foodRepo.loadFoodDayEntriesRangeSync(start, end)
        val weights = weightRepo.loadEntriesRangeSync(start, end)
        val days = dayStarts(start, end)

        val primary = when (metric) {
            DashboardTrendMetric.ACTIVE_ENERGY -> activeEnergy(days, activityEntries)
            DashboardTrendMetric.CALORIE_BALANCE -> calorieBalance(days, foodDays, activityDays)
            DashboardTrendMetric.NET_WEIGHT -> realWeightDelta(days, weights)
            DashboardTrendMetric.WEIGHT_PROJECTION -> projectedWeightDelta(days, foodDays, activityDays)
        }
        val secondary = if (metric == DashboardTrendMetric.WEIGHT_PROJECTION) {
            realWeightDelta(days, weights).filter { it.value != 0f || weights.isNotEmpty() }
        } else {
            emptyList()
        }
        val smooth = smooth(primary)
        return TrendUiModel(
            metric = metric,
            range = range,
            primary = primary,
            smooth = smooth,
            secondary = secondary,
            summary = summary(metric, primary, secondary),
            note = note(metric, start, end),
        )
    }

    private fun activeEnergy(days: List<Long>, entries: List<ActivityEntry>): List<TrendPoint> {
        val byDay = entries.groupBy { dayStart(it.startTime) }
        return days.map { day ->
            TrendPoint(day, byDay[day]?.sumOf { it.calories }?.toFloat() ?: 0f)
        }
    }

    private fun calorieBalance(
        days: List<Long>,
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<ActivityDayEntry>,
    ): List<TrendPoint> {
        val foodByDay = foodDays.associateBy { it.day }
        val expenditureByDay = activityDays.associateBy { it.startTime }
        return days.map { day ->
            val expenditure = expenditureByDay[day]?.totalCalories ?: 0f
            val consumed = foodByDay[day]?.calories?.toFloat() ?: expenditure
            TrendPoint(day, consumed - expenditure)
        }
    }

    private fun realWeightDelta(days: List<Long>, weights: List<WeightEntry>): List<TrendPoint> {
        if (weights.isEmpty()) return days.map { TrendPoint(it, 0f) }
        val sorted = weights.sortedBy { it.timeInMillis }
        val baseline = sorted.first().kg()
        return days.map { day ->
            val latest = sorted.lastOrNull { it.timeInMillis < day + ONE_DAY_MILLIS }?.kg() ?: baseline
            TrendPoint(day, (latest - baseline).toFloat())
        }
    }

    private fun projectedWeightDelta(
        days: List<Long>,
        foodDays: List<com.bruhascended.db.food.entities.DayEntry>,
        activityDays: List<ActivityDayEntry>,
    ): List<TrendPoint> {
        var cumulative = 0f
        return calorieBalance(days, foodDays, activityDays).map { point ->
            cumulative += point.value
            TrendPoint(point.time, cumulative / KCAL_PER_KG)
        }
    }

    private fun smooth(points: List<TrendPoint>): List<TrendPoint> {
        if (points.size < 3) return points
        val window = when {
            points.size > 730 -> 45
            points.size > 365 -> 30
            points.size > 120 -> 14
            points.size > 45 -> 7
            else -> 3
        }
        return points.mapIndexed { index, point ->
            val from = (index - window + 1).coerceAtLeast(0)
            val slice = points.subList(from, index + 1)
            TrendPoint(point.time, slice.map { it.value }.average().toFloat())
        }
    }

    private fun summary(
        metric: DashboardTrendMetric,
        primary: List<TrendPoint>,
        secondary: List<TrendPoint>,
    ): List<Pair<String, String>> {
        val values = primary.map { it.value }
        val total = values.sum()
        val latest = values.lastOrNull() ?: 0f
        val avg = values.average().takeUnless { it.isNaN() }?.toFloat() ?: 0f
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val delta = latest - (values.firstOrNull() ?: 0f)
        val unit = metric.unit
        val base = mutableListOf(
            "Current" to fmt(latest, unit),
            "Range delta" to fmt(delta, unit),
            "Daily average" to fmt(avg, unit),
            "Low / high" to "${fmt(min, unit)} / ${fmt(max, unit)}",
        )
        if (metric == DashboardTrendMetric.ACTIVE_ENERGY ||
            metric == DashboardTrendMetric.CALORIE_BALANCE
        ) {
            base += "Cumulative" to fmt(total, unit)
        }
        if (secondary.isNotEmpty()) {
            base += "Logged weight delta" to fmt(secondary.last().value, unit)
        }
        return base
    }

    private fun note(metric: DashboardTrendMetric, start: Date, end: Date): String {
        val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val source = when (metric) {
            DashboardTrendMetric.ACTIVE_ENERGY -> "activity sessions only"
            DashboardTrendMetric.CALORIE_BALANCE -> "food calories minus daily expenditure"
            DashboardTrendMetric.NET_WEIGHT -> "logged weight readings"
            DashboardTrendMetric.WEIGHT_PROJECTION -> "cumulative calorie balance at 7700 kcal/kg"
        }
        return "$source • ${fmt.format(start)} - ${fmt.format(end)}"
    }

    private fun startFor(range: TrendRange, end: Date): Date =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            time = end
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            when (range) {
                TrendRange.WEEK -> add(Calendar.DAY_OF_YEAR, -6)
                TrendRange.MONTH -> add(Calendar.MONTH, -1)
                TrendRange.YTD -> {
                    set(Calendar.MONTH, Calendar.JANUARY)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                TrendRange.YEAR -> add(Calendar.YEAR, -1)
                TrendRange.TWO_YEARS -> add(Calendar.YEAR, -2)
                TrendRange.THREE_YEARS -> add(Calendar.YEAR, -3)
                TrendRange.FIVE_YEARS -> add(Calendar.YEAR, -5)
            }
        }.time

    private fun endOfToday(): Date =
        Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

    private fun dayStarts(start: Date, end: Date): List<Long> {
        val out = mutableListOf<Long>()
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            time = start
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        while (cal.timeInMillis <= end.time) {
            out += cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return out
    }

    private fun dayStart(timeInMillis: Long): Long =
        Calendar.getInstance(TimeZone.getDefault()).run {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            this.timeInMillis
        }

    private fun WeightEntry.kg(): Double = weight * type.conversionRatio

    private fun fmt(value: Float, unit: String): String {
        val absValue = abs(value)
        val decimals = when {
            unit == "kg" -> 2
            absValue >= 100 -> 0
            else -> 1
        }
        val pattern = if (decimals == 0) "%.0f" else "%.${decimals}f"
        val rendered = String.format(Locale.getDefault(), pattern, value)
        return "$rendered $unit"
    }

    companion object {
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
        private const val KCAL_PER_KG = 7700f
    }
}
