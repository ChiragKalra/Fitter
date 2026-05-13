package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.NutrientCard
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.ui.theme.Blue100
import com.bruhascended.fitapp.ui.theme.Blue500
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import com.bruhascended.fitapp.ui.theme.Green200
import com.bruhascended.fitapp.ui.theme.Purple200
import com.bruhascended.fitapp.ui.theme.Red200
import com.bruhascended.fitapp.ui.theme.Yellow500
import com.bruhascended.fitapp.util.getWeekList
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {
    private val outerCircleDiameter = 200.dp
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val intent = Intent(activity, SettingsActivity::class.java)
        val view = ComposeView(requireContext())
        view.apply {
            val repo = PreferencesRepository(context)
            val nutrientGoals = repo.nutritionGoalsFlow
            val activityGoals = repo.activityGoalsFlow
            setContent {
                FitAppTheme {
                    // ── data state ──────────────────────────────────────────
                    val defaultList = getWeekList(context)
                    var energyExpList by remember { mutableStateOf(defaultList) }
                    var stepsList by remember { mutableStateOf(defaultList) }
                    var activeEnergyWeekList by remember { mutableStateOf(defaultList) }
                    var calorieBalanceWeekList by remember { mutableStateOf(defaultList) }
                    var weightDeltaWeekList by remember { mutableStateOf(defaultList) }
                    var projectedWeightWeekList by remember { mutableStateOf(defaultList) }
                    var proteinWeekList by remember { mutableStateOf(getWeekList(context)) }
                    var carbsWeekList by remember { mutableStateOf(getWeekList(context)) }
                    var fatWeekList by remember { mutableStateOf(getWeekList(context)) }
                    var sugarWeekList by remember { mutableStateOf(getWeekList(context)) }
                    var activityWeekDays by remember { mutableStateOf<List<DayEntry>>(emptyList()) }
                    var foodWeekDays by remember {
                        mutableStateOf<List<com.bruhascended.db.food.entities.DayEntry>>(emptyList())
                    }
                    var latestFoodLoggedAt by remember { mutableStateOf<Long?>(null) }
                    var latestWeightLoggedAt by remember { mutableStateOf<Long?>(null) }
                    var todayActivityData by remember { mutableStateOf(DayEntry(0L)) }
                    var todayNutrientData by remember {
                        mutableStateOf(com.bruhascended.db.food.entities.DayEntry(0L))
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    val actLd = viewModel.activityData
                    val activeLd = viewModel.activityEntries
                    val nutLd = viewModel.nutrientData
                    val foodWeekLd = viewModel.foodWeekDayEntries
                    val weightLd = viewModel.weightEntries
                    val latestFoodLd = viewModel.latestFoodEntry
                    val latestWeightLd = viewModel.latestWeightEntry

                    DisposableEffect(actLd, activeLd, nutLd, foodWeekLd, weightLd, latestFoodLd, latestWeightLd, lifecycleOwner) {
                        val activityObserver = Observer<List<DayEntry>> { list ->
                            activityWeekDays = list
                            energyExpList = viewModel.getLastWeekEnergyExp(list, energyExpList)
                            stepsList = viewModel.getLastWeekSteps(list, stepsList)
                            val tmpl = getWeekList(context)
                            calorieBalanceWeekList = viewModel.getCalorieBalance(foodWeekDays, activityWeekDays, tmpl)
                            projectedWeightWeekList = viewModel.getProjectedWeightDelta(foodWeekDays, activityWeekDays, tmpl)
                            todayActivityData = if (list.isNotEmpty()) list.last() else DayEntry(0L)
                        }
                        val activeObserver = Observer<List<com.bruhascended.db.activity.entities.ActivityEntry>> { list ->
                            activeEnergyWeekList = viewModel.getLastWeekActiveEnergy(list, getWeekList(context))
                        }
                        val nutrientObserver = Observer<com.bruhascended.db.food.entities.DayEntry?> { entry ->
                            todayNutrientData = entry ?: viewModel.defaultNutrientData
                        }
                        val foodWeekObserver = Observer<List<com.bruhascended.db.food.entities.DayEntry>> { list ->
                            foodWeekDays = list
                            val tmpl = getWeekList(context)
                            proteinWeekList = viewModel.getLastWeekNutrientGrams(list, tmpl, NutrientType.Protein)
                            carbsWeekList = viewModel.getLastWeekNutrientGrams(list, tmpl, NutrientType.Carbs)
                            fatWeekList = viewModel.getLastWeekNutrientGrams(list, tmpl, NutrientType.Fat)
                            sugarWeekList = viewModel.getLastWeekNutrientGrams(list, tmpl, NutrientType.AddedSugar)
                            calorieBalanceWeekList = viewModel.getCalorieBalance(foodWeekDays, activityWeekDays, tmpl)
                            projectedWeightWeekList = viewModel.getProjectedWeightDelta(foodWeekDays, activityWeekDays, tmpl)
                        }
                        val weightObserver = Observer<List<com.bruhascended.db.weight.entities.WeightEntry>> { list ->
                            weightDeltaWeekList = viewModel.getWeightDelta(list, getWeekList(context))
                        }
                        val latestFoodObserver = Observer<Entry?> { entry -> latestFoodLoggedAt = entry?.timeInMillis }
                        val latestWeightObserver = Observer<WeightEntry?> { entry -> latestWeightLoggedAt = entry?.timeInMillis }
                        actLd?.observe(lifecycleOwner, activityObserver)
                        activeLd?.observe(lifecycleOwner, activeObserver)
                        nutLd?.observe(lifecycleOwner, nutrientObserver)
                        foodWeekLd?.observe(lifecycleOwner, foodWeekObserver)
                        weightLd?.observe(lifecycleOwner, weightObserver)
                        latestFoodLd?.observe(lifecycleOwner, latestFoodObserver)
                        latestWeightLd?.observe(lifecycleOwner, latestWeightObserver)
                        onDispose {
                            actLd?.removeObserver(activityObserver)
                            activeLd?.removeObserver(activeObserver)
                            nutLd?.removeObserver(nutrientObserver)
                            foodWeekLd?.removeObserver(foodWeekObserver)
                            weightLd?.removeObserver(weightObserver)
                            latestFoodLd?.removeObserver(latestFoodObserver)
                            latestWeightLd?.removeObserver(latestWeightObserver)
                        }
                    }

                    // ── layout state ─────────────────────────────────────────
                    val dashConfig by produceState(viewModel.dashboardUiConfig.value, viewModel) {
                        viewModel.dashboardUiConfig.collect { value = it }
                    }

                    var selectedSection by remember { mutableStateOf<DashboardSection?>(null) }
                    val isEditMode by remember { derivedStateOf { selectedSection != null } }

                    val draftWidths: SnapshotStateMap<DashboardSection, Float> = remember {
                        mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.widthFractions) }
                    }
                    val draftHeightScales: SnapshotStateMap<DashboardSection, Float> = remember {
                        mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.heightScales) }
                    }
                    val draftOrder: SnapshotStateList<DashboardSection> = remember {
                        mutableStateListOf<DashboardSection>().also { it.addAll(dashConfig.order) }
                    }

                    LaunchedEffect(dashConfig) {
                        if (selectedSection == null) {
                            draftWidths.clear(); draftWidths.putAll(dashConfig.widthFractions)
                            draftHeightScales.clear(); draftHeightScales.putAll(dashConfig.heightScales)
                            draftOrder.clear(); draftOrder.addAll(dashConfig.order)
                        }
                    }

                    BackHandler(enabled = isEditMode) { selectedSection = null }

                    // Shared map of card bounds in window coordinates — used for drag-to-reorder hit testing
                    val cardBounds = remember { mutableMapOf<DashboardSection, Rect>() }

                    val gridColumns = dashConfig.gridSize.columns
                    val visibleSections by remember(draftOrder, dashConfig.hiddenIds) {
                        derivedStateOf { draftOrder.filter { it !in dashConfig.hiddenIds } }
                    }

                    // Pack visible sections into rows based on their widths.
                    // Each row is a list of sections whose widths sum to <= 1.0.
                    // This is the core of the "launcher-style" layout.
                    val rows by remember(visibleSections, draftWidths, gridColumns) {
                        derivedStateOf {
                            packIntoRows(
                                sections = visibleSections,
                                widthOf = { s ->
                                    widthForGridSpan(
                                        gridSpanForWidth(draftWidths[s] ?: dashConfig.widthFor(s), gridColumns),
                                        gridColumns,
                                    )
                                },
                            )
                        }
                    }

                    val listBottomPadding = dimensionResource(R.dimen.main_bottom_nav_height) + 160.dp
                    val listState = rememberLazyListState()

                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 8.dp, end = 8.dp, bottom = listBottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        userScrollEnabled = !isEditMode,
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .background(MaterialTheme.colors.background),
                    ) {
                        // Settings icon — scrolls with content
                        item(key = "dashboard_settings_bar") {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                IconButton(onClick = { startActivity(intent) }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_settings),
                                        tint = MaterialTheme.colors.onSurface,
                                        contentDescription = stringResource(R.string.settings),
                                    )
                                }
                            }
                        }

                        // Greeting card — always full width
                        item(key = DASHBOARD_GREETING_KEY) {
                            DashboardGreetingCard(
                                latestFoodLoggedAt = latestFoodLoggedAt,
                                latestWeightLoggedAt = latestWeightLoggedAt,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        // Widget rows — each row packs cards side-by-side
                        items(
                            count = rows.size,
                            key = { rowIndex -> rows[rowIndex].joinToString(",") { it.persistenceId } },
                        ) { rowIndex ->
                            val rowSections = rows[rowIndex]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowSections.forEachIndexed { colIndex, section ->
                                    val committedWidth = widthForGridSpan(
                                        gridSpanForWidth(draftWidths[section] ?: dashConfig.widthFor(section), gridColumns),
                                        gridColumns,
                                    )
                                    val committedHeight = draftHeightScales[section] ?: dashConfig.heightScaleFor(section)

                                    DashboardWidgetCard(
                                        section = section,
                                        selected = selectedSection == section,
                                        committedWidthFraction = committedWidth,
                                        committedHeightScale = committedHeight,
                                        cardBounds = cardBounds,
                                        onClick = {
                                            if (selectedSection == section) {
                                                selectedSection = null
                                            } else {
                                                dashboardMetricFor(section)?.let { metric ->
                                                    startActivity(DashboardTrendActivity.intent(context, metric))
                                                }
                                            }
                                        },
                                        onLongPress = {
                                            selectedSection = section
                                            draftWidths.clear(); draftWidths.putAll(dashConfig.widthFractions)
                                            draftHeightScales.clear(); draftHeightScales.putAll(dashConfig.heightScales)
                                        },
                                        onReorder = { fromSection, toSection ->
                                            val fi = draftOrder.indexOf(fromSection)
                                            val ti = draftOrder.indexOf(toSection)
                                            if (fi != -1 && ti != -1 && fi != ti) {
                                                draftOrder.add(ti, draftOrder.removeAt(fi))
                                            }
                                        },
                                        onReorderEnd = {
                                            viewModel.saveDashboardLayout(draftOrder.toList(), dashConfig.hiddenIds)
                                        },
                                        onShapeChangeFinished = { width, height ->
                                            val snappedWidth = widthForGridSpan(gridSpanForWidth(width, gridColumns), gridColumns)
                                            val snappedHeight = heightForGridUnits(height, dashConfig.gridSize.heightUnits)
                                            if (snappedWidth >= DashboardUiConfig.MAX_CARD_WIDTH_FRACTION) {
                                                draftWidths.remove(section)
                                            } else {
                                                draftWidths[section] = snappedWidth
                                            }
                                            if (snappedHeight == DashboardUiConfig.DEFAULT_CARD_HEIGHT_SCALE) {
                                                draftHeightScales.remove(section)
                                            } else {
                                                draftHeightScales[section] = snappedHeight
                                            }
                                            viewModel.saveDashboardCardShape(section, snappedWidth, snappedHeight)
                                        },
                                        modifier = Modifier.weight(committedWidth),
                                    ) {
                                        SectionContent(
                                            section = section,
                                            outerCircleDiameter = outerCircleDiameter,
                                            todayActivityData = todayActivityData,
                                            todayNutrientData = todayNutrientData,
                                            activityGoals = activityGoals,
                                            nutrientGoals = nutrientGoals,
                                            stepsList = stepsList,
                                            energyExpList = energyExpList,
                                            activeEnergyWeekList = activeEnergyWeekList,
                                            calorieBalanceWeekList = calorieBalanceWeekList,
                                            weightDeltaWeekList = weightDeltaWeekList,
                                            projectedWeightWeekList = projectedWeightWeekList,
                                            proteinWeekList = proteinWeekList,
                                            carbsWeekList = carbsWeekList,
                                            fatWeekList = fatWeekList,
                                            sugarWeekList = sugarWeekList,
                                            context = context,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return view
    }

    // ── Row-packing algorithm ────────────────────────────────────────────────
    // Groups sections into rows such that each row's total width <= 1.0.
    // This is what makes resize/reorder work like a launcher — cards flow
    // naturally and neighbors are pushed to the next row when needed.
    private fun packIntoRows(
        sections: List<DashboardSection>,
        widthOf: (DashboardSection) -> Float,
    ): List<List<DashboardSection>> {
        val rows = mutableListOf<List<DashboardSection>>()
        val currentRow = mutableListOf<DashboardSection>()
        var rowWidth = 0f
        for (section in sections) {
            val w = widthOf(section).coerceIn(0.01f, 1f)
            if (currentRow.isNotEmpty() && rowWidth + w > 1f + 0.01f) {
                rows.add(currentRow.toList())
                currentRow.clear()
                rowWidth = 0f
            }
            currentRow.add(section)
            rowWidth += w
        }
        if (currentRow.isNotEmpty()) rows.add(currentRow.toList())
        return rows
    }

    // ── DashboardWidgetCard ──────────────────────────────────────────────────
    @Composable
    private fun DashboardWidgetCard(
        section: DashboardSection,
        selected: Boolean,
        committedWidthFraction: Float,
        committedHeightScale: Float,
        cardBounds: MutableMap<DashboardSection, Rect>,
        onClick: () -> Unit,
        onLongPress: () -> Unit,
        onReorder: (from: DashboardSection, to: DashboardSection) -> Unit,
        onReorderEnd: () -> Unit,
        onShapeChangeFinished: (Float, Float) -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        var draftWidth by remember(committedWidthFraction) { mutableStateOf(committedWidthFraction) }
        var draftHeightScale by remember(committedHeightScale) { mutableStateOf(committedHeightScale) }

        val shape = RoundedCornerShape(12.dp)
        val baseHeight = baseCardHeight(section)

        BoxWithConstraints(
            modifier = modifier.onGloballyPositioned { coords ->
                cardBounds[section] = coords.boundsInWindow()
            },
            contentAlignment = Alignment.TopStart,
        ) {
            val density = LocalDensity.current
            val containerWidthPx = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
            val fullRowWidthPx = (containerWidthPx / committedWidthFraction.coerceAtLeast(0.01f))
            val baseHeightPx = with(density) { baseHeight.toPx() }.coerceAtLeast(1f)
            val targetHeight = baseHeight * DashboardUiConfig.clampHeightScale(draftHeightScale)

            val onShapeChange: (Float, Float) -> Unit = { w, h ->
                draftWidth = w
                draftHeightScale = h
            }

            Box(modifier = Modifier.fillMaxWidth().height(targetHeight)) {
                // Card content — tap to navigate, long-press+drag to reorder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(targetHeight)
                        .clip(shape)
                        .pointerInput(section, selected) {
                            if (selected) {
                                // Already in edit mode: plain drag reorders immediately
                                detectDragGestures(
                                    onDragEnd = { onReorderEnd() },
                                    onDragCancel = { onReorderEnd() },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val bounds = cardBounds[section] ?: return@detectDragGestures
                                        val windowPos = androidx.compose.ui.geometry.Offset(
                                            bounds.left + change.position.x,
                                            bounds.top + change.position.y,
                                        )
                                        val target = cardBounds.entries
                                            .firstOrNull { (s, r) -> s != section && r.contains(windowPos) }
                                            ?.key
                                        if (target != null) onReorder(section, target)
                                    },
                                )
                            } else {
                                // Not in edit mode: long-press selects + starts drag in one gesture
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { onLongPress() },
                                    onDragEnd = { onReorderEnd() },
                                    onDragCancel = { onReorderEnd() },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        val bounds = cardBounds[section] ?: return@detectDragGesturesAfterLongPress
                                        val windowPos = androidx.compose.ui.geometry.Offset(
                                            bounds.left + change.position.x,
                                            bounds.top + change.position.y,
                                        )
                                        val target = cardBounds.entries
                                            .firstOrNull { (s, r) -> s != section && r.contains(windowPos) }
                                            ?.key
                                        if (target != null) onReorder(section, target)
                                    },
                                )
                            }
                        }
                        .pointerInput(section, selected) {
                            // Tap only fires when not dragging
                            if (!selected) {
                                detectTapGestures(onTap = { onClick() })
                            } else {
                                detectTapGestures(onTap = { onClick() })
                            }
                        },
                ) {
                    content()
                }

                // Edit-mode overlay: border + resize handles only
                if (selected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(BorderStroke(2.dp, MaterialTheme.colors.primary), shape),
                    )
                    DashboardResizeHandle(
                        contentDescription = stringResource(R.string.dashboard_resize_left_a11y),
                        horizontalDirection = -1f, verticalDirection = 0f,
                        containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                        widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                        heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                        onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinished,
                        modifier = Modifier.align(Alignment.CenterStart).offset(x = 20.dp),
                    )
                    DashboardResizeHandle(
                        contentDescription = stringResource(R.string.dashboard_resize_right_a11y),
                        horizontalDirection = 1f, verticalDirection = 0f,
                        containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                        widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                        heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                        onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinished,
                        modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-30).dp),
                    )
                    DashboardResizeHandle(
                        contentDescription = stringResource(R.string.dashboard_resize_top_a11y),
                        horizontalDirection = 0f, verticalDirection = -1f,
                        containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                        widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                        heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                        onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinished,
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = (-9).dp),
                    )
                    DashboardResizeHandle(
                        contentDescription = stringResource(R.string.dashboard_resize_bottom_a11y),
                        horizontalDirection = 0f, verticalDirection = 1f,
                        containerWidthPx = fullRowWidthPx, baseHeightPx = baseHeightPx,
                        widthFraction = DashboardUiConfig.clampWidthFraction(draftWidth),
                        heightScale = DashboardUiConfig.clampHeightScale(draftHeightScale),
                        onShapeChange = onShapeChange, onShapeChangeFinished = onShapeChangeFinished,
                        modifier = Modifier.align(Alignment.BottomCenter).offset(y = 9.dp),
                    )
                }
            }
        }
    }

    // ── DashboardResizeHandle ────────────────────────────────────────────────
    @Composable
    private fun DashboardResizeHandle(
        contentDescription: String,
        horizontalDirection: Float,
        verticalDirection: Float,
        containerWidthPx: Float,
        baseHeightPx: Float,
        widthFraction: Float,
        heightScale: Float,
        onShapeChange: (Float, Float) -> Unit,
        onShapeChangeFinished: (Float, Float) -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val latestWidth by rememberUpdatedState(widthFraction)
        val latestHeightScale by rememberUpdatedState(heightScale)
        var dragStartWidth by remember { mutableStateOf(widthFraction) }
        var dragStartHeightScale by remember { mutableStateOf(heightScale) }
        var dragDistanceXPx by remember { mutableStateOf(0f) }
        var dragDistanceYPx by remember { mutableStateOf(0f) }
        var lastWidth by remember { mutableStateOf(widthFraction) }
        var lastHeightScale by remember { mutableStateOf(heightScale) }
        val isHorizontalHandle = horizontalDirection != 0f
        val handleColor = MaterialTheme.colors.primary.copy(alpha = 0.2f)
        val gripColor = MaterialTheme.colors.primary.copy(alpha = 0.82f)

        Box(
            modifier = modifier
                .width(if (isHorizontalHandle) 18.dp else 58.dp)
                .height(if (isHorizontalHandle) 58.dp else 18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(handleColor)
                .border(BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.45f)), RoundedCornerShape(9.dp))
                .pointerInput(horizontalDirection, verticalDirection, containerWidthPx, baseHeightPx) {
                    detectDragGestures(
                        onDragStart = {
                            dragStartWidth = latestWidth; dragStartHeightScale = latestHeightScale
                            dragDistanceXPx = 0f; dragDistanceYPx = 0f
                            lastWidth = latestWidth; lastHeightScale = latestHeightScale
                        },
                        onDragEnd = { onShapeChangeFinished(lastWidth, lastHeightScale) },
                        onDragCancel = { onShapeChangeFinished(lastWidth, lastHeightScale) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragDistanceXPx += dragAmount.x
                            dragDistanceYPx += dragAmount.y
                            val nextWidth = DashboardUiConfig.clampWidthFraction(
                                dragStartWidth + horizontalDirection * dragDistanceXPx / containerWidthPx,
                            )
                            val nextHeight = DashboardUiConfig.clampHeightScale(
                                dragStartHeightScale + verticalDirection * dragDistanceYPx / baseHeightPx,
                            )
                            lastWidth = nextWidth; lastHeightScale = nextHeight
                            onShapeChange(nextWidth, nextHeight)
                        },
                    )
                }
                .semantics { role = Role.Button; this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(if (isHorizontalHandle) 3.dp else 30.dp)
                    .height(if (isHorizontalHandle) 30.dp else 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(gripColor),
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private fun baseCardHeight(section: DashboardSection): androidx.compose.ui.unit.Dp =
        when (section) {
            DashboardSection.SUMMARY_RING -> 220.dp
            DashboardSection.TODAY_STATS -> 142.dp
            DashboardSection.NUTRIENTS -> 150.dp
            else -> 180.dp
        }

    private fun dashboardMetricFor(section: DashboardSection): DashboardTrendMetric? =
        when (section) {
            DashboardSection.ACTIVE_ENERGY -> DashboardTrendMetric.ACTIVE_ENERGY
            DashboardSection.CALORIE_BALANCE -> DashboardTrendMetric.CALORIE_BALANCE
            DashboardSection.WEIGHT_DELTA -> DashboardTrendMetric.NET_WEIGHT
            DashboardSection.WEIGHT_PROJECTION -> DashboardTrendMetric.WEIGHT_PROJECTION
            else -> null
        }

    private fun gridSpanForWidth(widthFraction: Float, gridColumns: Int): Int =
        (DashboardUiConfig.clampWidthFraction(widthFraction) * gridColumns)
            .roundToInt().coerceIn(1, gridColumns)

    private fun widthForGridSpan(span: Int, gridColumns: Int): Float =
        (span.coerceIn(1, gridColumns).toFloat() / gridColumns)
            .coerceIn(DashboardUiConfig.MIN_CARD_WIDTH_FRACTION, DashboardUiConfig.MAX_CARD_WIDTH_FRACTION)

    private fun heightForGridUnits(heightScale: Float, heightUnits: Int): Float {
        val clamped = DashboardUiConfig.clampHeightScale(heightScale)
        val step = 1f / heightUnits.coerceAtLeast(1)
        return (clamped / step).roundToInt().coerceAtLeast(1).times(step)
            .coerceIn(DashboardUiConfig.MIN_CARD_HEIGHT_SCALE, DashboardUiConfig.MAX_CARD_HEIGHT_SCALE)
    }

    // ── Greeting card ────────────────────────────────────────────────────────
    @Composable
    private fun DashboardGreetingCard(
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

    // ── SectionContent ───────────────────────────────────────────────────────
    @Composable
    private fun SectionContent(
        section: DashboardSection,
        outerCircleDiameter: androidx.compose.ui.unit.Dp,
        todayActivityData: DayEntry,
        todayNutrientData: com.bruhascended.db.food.entities.DayEntry,
        activityGoals: PreferencesRepository.ActivityPreferences,
        nutrientGoals: PreferencesRepository.NutritionPreferences,
        stepsList: List<com.bruhascended.fitapp.util.BarGraphData>,
        energyExpList: List<com.bruhascended.fitapp.util.BarGraphData>,
        activeEnergyWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        calorieBalanceWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        weightDeltaWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        projectedWeightWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        proteinWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        carbsWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        fatWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        sugarWeekList: List<com.bruhascended.fitapp.util.BarGraphData>,
        context: android.content.Context,
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
    private fun TappableOverviewCard(data: List<com.bruhascended.fitapp.util.BarGraphData>, context: android.content.Context, title: String, unit: String, goal: Long, color: Color) {
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
    fun CurrentDayItem(unit: String = "", data: String, painter: Painter, color: Color, description: String) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painter = painter, tint = color, contentDescription = description, modifier = Modifier.size(24.dp).padding(bottom = 4.dp))
            Text(text = "$data $unit", color = MaterialTheme.colors.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }

    private companion object {
        private const val DASHBOARD_GREETING_KEY = "dashboard_greeting"
        private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
