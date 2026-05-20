package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import kotlinx.coroutines.delay
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
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
                    // ── data state (unchanged) ────────────────────────────────
                    val stepsList by viewModel.stepsListData.collectAsState()
                    val energyExpList by viewModel.energyExpListData.collectAsState()
                    val activeEnergyWeekList by viewModel.activeEnergyListData.collectAsState()
                    val calorieBalanceWeekList by viewModel.calorieBalanceListData.collectAsState()
                    val weightDeltaWeekList by viewModel.weightDeltaListData.collectAsState()
                    val projectedWeightWeekList by viewModel.projectedWeightListData.collectAsState()
                    val proteinWeekList by viewModel.proteinListData.collectAsState()
                    val carbsWeekList by viewModel.carbsListData.collectAsState()
                    val fatWeekList by viewModel.fatListData.collectAsState()
                    val sugarWeekList by viewModel.sugarListData.collectAsState()

                    var latestFoodLoggedAt by remember { mutableStateOf<Long?>(null) }
                    var latestWeightLoggedAt by remember { mutableStateOf<Long?>(null) }
                    var todayActivityData by remember { mutableStateOf(DayEntry(0L)) }
                    var todayNutrientData by remember {
                        mutableStateOf(com.bruhascended.db.food.entities.DayEntry(0L))
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    val actLd = viewModel.activityData
                    val nutLd = viewModel.nutrientData
                    val latestFoodLd = viewModel.latestFoodEntry
                    val latestWeightLd = viewModel.latestWeightEntry

                    DisposableEffect(actLd, nutLd, latestFoodLd, latestWeightLd, lifecycleOwner) {
                        val activityObserver = Observer<List<DayEntry>> { list ->
                            todayActivityData = list.lastOrNull {
                                viewModel.dayStartMillis(it.date) == viewModel.dayStartMillis(Date())
                            } ?: DayEntry(0L)
                        }
                        val nutrientObserver = Observer<com.bruhascended.db.food.entities.DayEntry?> { entry ->
                            todayNutrientData = entry ?: viewModel.defaultNutrientData
                        }
                        val latestFoodObserver = Observer<Entry?> { entry ->
                            latestFoodLoggedAt = entry?.date?.time
                        }
                        val latestWeightObserver = Observer<WeightEntry?> { entry ->
                            latestWeightLoggedAt = entry?.timeInMillis
                        }

                        actLd?.observe(lifecycleOwner, activityObserver)
                        nutLd?.observe(lifecycleOwner, nutrientObserver)
                        latestFoodLd?.observe(lifecycleOwner, latestFoodObserver)
                        latestWeightLd?.observe(lifecycleOwner, latestWeightObserver)

                        onDispose {
                            actLd?.removeObserver(activityObserver)
                            nutLd?.removeObserver(nutrientObserver)
                            latestFoodLd?.removeObserver(latestFoodObserver)
                            latestWeightLd?.removeObserver(latestWeightObserver)
                        }
                    }

                    // ── layout config ─────────────────────────────────────────
                    val dashConfig by viewModel.dashboardUiConfig.collectAsState()
                    val gridColumns = dashConfig.gridSize.columns

                    val draftOrder: SnapshotStateList<DashboardSection> = remember {
                        mutableStateListOf<DashboardSection>().also { it.addAll(dashConfig.order) }
                    }
                    val draftWidths: SnapshotStateMap<DashboardSection, Float> = remember {
                        mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.widthFractions) }
                    }
                    val draftHeightScales: SnapshotStateMap<DashboardSection, Float> = remember {
                        mutableStateMapOf<DashboardSection, Float>().also { it.putAll(dashConfig.heightScales) }
                    }

                    // ── drag state ────────────────────────────────────────────
                    val dragState = remember { DashboardDragState() }
                    val cardBounds = remember { mutableMapOf<DashboardSection, Rect>() }
                    var removeZoneBounds by remember { mutableStateOf(Rect.Zero) }
                    var parentBoundsInWindow by remember { mutableStateOf(Rect.Zero) }
                    val androidView = LocalView.current
                    val density = LocalDensity.current

                    // ── resize / selected state ───────────────────────────────
                    var selectedSection by remember { mutableStateOf<DashboardSection?>(null) }
                    BackHandler(enabled = selectedSection != null) { selectedSection = null }

                    // Exclude left/right screen edges from system back gesture
                    // while a card is selected for resize (covers only the card's y-range).
                    DisposableEffect(selectedSection) {
                        val section = selectedSection
                        if (section != null) {
                            val bounds = cardBounds[section]
                            if (bounds != null) {
                                val viewLocation = IntArray(2)
                                androidView.getLocationOnScreen(viewLocation)
                                val viewTop = viewLocation[1]
                                val cardTopInView = (bounds.top - viewTop).toInt().coerceAtLeast(0)
                                val cardBottomInView = (bounds.bottom - viewTop).toInt().coerceAtMost(androidView.height)
                                val edgeWidthPx = (40 * density.density).toInt()
                                androidView.systemGestureExclusionRects = listOf(
                                    android.graphics.Rect(0, cardTopInView, edgeWidthPx, cardBottomInView),
                                    android.graphics.Rect(
                                        androidView.width - edgeWidthPx, cardTopInView,
                                        androidView.width, cardBottomInView,
                                    ),
                                )
                            }
                        }
                        onDispose {
                            androidView.systemGestureExclusionRects = emptyList()
                        }
                    }

                    LaunchedEffect(dashConfig) {
                        if (!dragState.isDragging) {
                            draftOrder.clear(); draftOrder.addAll(dashConfig.order)
                            draftWidths.clear(); draftWidths.putAll(dashConfig.widthFractions)
                            draftHeightScales.clear(); draftHeightScales.putAll(dashConfig.heightScales)
                        }
                    }

                    val visibleSections by remember(draftOrder, dashConfig.hiddenIds) {
                        derivedStateOf { draftOrder.filter { it !in dashConfig.hiddenIds } }
                    }

                    val rows by remember {
                        derivedStateOf {
                            packIntoRows(
                                sections = visibleSections,
                                widthOf = { s ->
                                    // Use committed widths for row packing — NOT draft widths.
                                    // Draft width changes during resize would reflow rows,
                                    // destroying the card composable and killing the gesture.
                                    // Rows reflow on resize finish when dashConfig updates.
                                    widthForGridSpan(
                                        gridSpanForWidth(dashConfig.widthFor(s), gridColumns),
                                        gridColumns,
                                    )
                                },
                            )
                        }
                    }

                    val rowKeys by remember {
                        derivedStateOf {
                            rows.map { row -> row.joinToString(",") { it.persistenceId } }
                        }
                    }

                    val listState = rememberLazyListState()
                    val listBottomPadding = dimensionResource(R.dimen.main_bottom_nav_height) + 160.dp

                    // ── auto-scroll at edges during drag ──────────────────────
                    AutoScrollDuringDrag(dragState, listState, parentBoundsInWindow.top, parentBoundsInWindow.bottom, density)

                    // ── hit-test: reorder + remove zone ───────────────────────
                    LaunchedEffect(dragState.fingerWindowOffset, dragState.isDragging) {
                        if (!dragState.isDragging) return@LaunchedEffect
                        val dragged = dragState.draggedSection ?: return@LaunchedEffect
                        val finger = dragState.fingerWindowOffset

                        dragState.hoveringOverRemoveZone =
                            removeZoneBounds != Rect.Zero && removeZoneBounds.contains(finger)

                        if (!dragState.hoveringOverRemoveZone) {
                            val target = cardBounds.entries
                                .firstOrNull { (s, r) -> s != dragged && r.contains(finger) }
                                ?.key
                            if (target != null) {
                                val fi = draftOrder.indexOf(dragged)
                                val ti = draftOrder.indexOf(target)
                                if (fi != -1 && ti != -1 && fi != ti) {
                                    draftOrder.add(ti, draftOrder.removeAt(fi))
                                    androidView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                }
                            }
                        }
                    }

                    // ── drag end handler ───────────────────────────────────────
                    val currentDashConfig by remember { derivedStateOf { dashConfig } }
                    val commitDragEnd: () -> Unit = {
                        val dragged = dragState.draggedSection
                        if (dragged != null && dragState.hoveringOverRemoveZone) {
                            val newHidden = currentDashConfig.hiddenIds + dragged
                            viewModel.saveDashboardLayout(draftOrder.toList(), newHidden)
                            androidView.performHapticFeedback(HapticFeedbackConstants.REJECT)
                        } else {
                            viewModel.saveDashboardLayout(draftOrder.toList(), currentDashConfig.hiddenIds)
                            androidView.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        dragState.reset()
                    }

                    // Dragged card dimensions — captured at drag start
                    val draggedW = with(density) { dragState.initialCardWidthPx.toDp() }
                    val draggedH = with(density) { dragState.initialCardHeightPx.toDp() }

                    // Animated scale: zoom-out effect on long-press (center origin = no translation)
                    val contentScale by animateFloatAsState(
                        targetValue = if (dragState.isDragging) 0.92f else 1f,
                        animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                        label = "content_scale",
                    )

                    // ── compose layout ─────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                            .onGloballyPositioned { coords ->
                                parentBoundsInWindow = coords.boundsInWindow()
                            }
                            // Parent-level pointer handler: tracks drag movement + release.
                            // Runs at PointerEventPass.Final so it sees events AFTER children,
                            // but since this always runs, it won't die on child recomposition.
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Final)
                                        if (!dragState.isDragging) continue

                                        for (change in event.changes) {
                                            if (change.pressed) {
                                                // Finger still down — update drag position
                                                val windowX = parentBoundsInWindow.left + change.position.x
                                                val windowY = parentBoundsInWindow.top + change.position.y
                                                dragState.updateDrag(
                                                    Offset(windowX, windowY),
                                                    change.position.y - change.previousPosition.y,
                                                )
                                                change.consume()
                                            } else if (change.previousPressed) {
                                                // Finger released — check if it was a real drag or just a long-press
                                                if (dragState.hasMoved) {
                                                    // Real drag → commit reorder
                                                    commitDragEnd()
                                                } else {
                                                    // Long press without movement → enter resize mode
                                                    val section = dragState.draggedSection
                                                    dragState.reset()
                                                    selectedSection = section
                                                }
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            },
                    ) {
                        // Scrollable content — zooms out during drag, center origin = pure resize
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 8.dp, end = 8.dp,
                                bottom = listBottomPadding,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .graphicsLayer {
                                    scaleX = contentScale
                                    scaleY = contentScale
                                    // Default TransformOrigin.Center — shrinks uniformly, no directional jump
                                },
                        ) {
                            item(key = "dashboard_settings_bar") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .animateItemPlacement(spring(stiffness = Spring.StiffnessMediumLow)),
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

                            item(key = DASHBOARD_GREETING_KEY) {
                                DashboardGreetingCard(
                                    latestFoodLoggedAt = latestFoodLoggedAt,
                                    latestWeightLoggedAt = latestWeightLoggedAt,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItemPlacement(spring(stiffness = Spring.StiffnessMediumLow)),
                                )
                            }

                            items(
                                count = rows.size,
                                key = { rowIndex -> rowKeys[rowIndex] },
                            ) { rowIndex ->
                                val rowSections = rows[rowIndex]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateItemPlacement(
                                            spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow,
                                            ),
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    rowSections.forEach { section ->
                                        // Use draft values (updated live during resize) with spring animation
                                        val liveWidth = draftWidths[section] ?: dashConfig.widthFor(section)
                                        val targetWidth = widthForGridSpan(
                                            gridSpanForWidth(liveWidth, gridColumns),
                                            gridColumns,
                                        )
                                        val animatedWeight by animateFloatAsState(
                                            targetValue = targetWidth,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                            label = "card_weight_${section.persistenceId}",
                                        )
                                        val liveHeight = draftHeightScales[section] ?: dashConfig.heightScaleFor(section)
                                        val baseHeight = 180.dp
                                        val targetHeightDp = baseHeight * DashboardUiConfig.clampHeightScale(liveHeight)
                                        val heightDp by animateDpAsState(
                                            targetValue = targetHeightDp,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                            label = "card_height_${section.persistenceId}",
                                        )

                                        DashboardWidgetCard(
                                            section = section,
                                            isBeingDragged = dragState.draggedSection == section,
                                            selected = selectedSection == section,
                                            committedWidthFraction = targetWidth,
                                            committedHeightScale = liveHeight,
                                            gridColumns = gridColumns,
                                            heightUnits = dashConfig.gridSize.heightUnits,
                                            heightDp = heightDp,
                                            cardBounds = cardBounds,
                                            onClick = {
                                                if (selectedSection == section) {
                                                    selectedSection = null
                                                } else if (selectedSection != null) {
                                                    selectedSection = section
                                                } else {
                                                    dashboardMetricFor(section)?.let { metric ->
                                                        startActivity(DashboardTrendActivity.intent(context, metric))
                                                    }
                                                }
                                            },
                                            onLongPress = {
                                                val bounds = cardBounds[section] ?: return@DashboardWidgetCard
                                                selectedSection = null
                                                dragState.startLift(
                                                    section,
                                                    Offset(bounds.center.x, bounds.center.y),
                                                    bounds.top,
                                                    Offset(bounds.width / 2f, bounds.height / 2f),
                                                    bounds.width,
                                                    bounds.height,
                                                )
                                                dragState.beginDrag()
                                            },
                                            onLiveResize = { w, h ->
                                                // Update draft maps during drag → recomposition with animated sizes
                                                draftWidths[section] = w
                                                draftHeightScales[section] = h
                                            },
                                            onShapeChangeFinished = { snappedWidth, snappedHeight ->
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
                                            modifier = Modifier.weight(animatedWeight),
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

                        // Remove drop zone — slides in from top
                        DashboardRemoveDropZone(
                            visible = dragState.isDragging,
                            isHovering = dragState.hoveringOverRemoveZone,
                            onBoundsChanged = { removeZoneBounds = it },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .zIndex(10f),
                        )

                        // Floating drag overlay
                        if (dragState.draggedSection != null) {
                            DashboardDragOverlay(
                                dragState = dragState,
                                cardWidthDp = draggedW,
                                cardHeightDp = draggedH,
                                parentTopInWindow = parentBoundsInWindow.top,
                            ) {
                                val section = dragState.draggedSection ?: return@DashboardDragOverlay
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
        return view
    }
}

/** Continuously scrolls the list when the finger is near top/bottom edges during a drag. */
@Composable
private fun AutoScrollDuringDrag(
    dragState: DashboardDragState,
    listState: LazyListState,
    parentTop: Float,
    parentBottom: Float,
    density: androidx.compose.ui.unit.Density,
) {
    LaunchedEffect(dragState.isDragging) {
        if (!dragState.isDragging) return@LaunchedEffect
        val edgeThresholdPx = with(density) { 80.dp.toPx() }
        val maxScrollSpeed = 24f
        while (dragState.isDragging) {
            val fingerY = dragState.fingerWindowOffset.y
            val distFromTop = fingerY - parentTop
            val distFromBottom = parentBottom - fingerY
            val scrollAmount = when {
                distFromTop in 0f..edgeThresholdPx ->
                    -maxScrollSpeed * (1f - distFromTop / edgeThresholdPx)
                distFromBottom in 0f..edgeThresholdPx ->
                    maxScrollSpeed * (1f - distFromBottom / edgeThresholdPx)
                else -> 0f
            }
            if (scrollAmount != 0f) {
                listState.scroll { scrollBy(scrollAmount) }
            }
            delay(16)
        }
    }
}
