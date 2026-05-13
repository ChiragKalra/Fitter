package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.types.NutrientType
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentDashboardBinding
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.NutrientCard
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.main.FabPresenter
import com.bruhascended.fitapp.ui.main.MainActivity
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
import java.util.Date
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

                    // ── layout state ─────────────────────────────────────────
                    val dashConfig by viewModel.dashboardUiConfig.collectAsState()

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

                    // Drag state — tracks which card is being dragged and its current finger offset
                    var dragSection by remember { mutableStateOf<DashboardSection?>(null) }
                    var dragOffset by remember { mutableStateOf(Offset.Zero) }

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

                    // Pre-compute stable keys so we don't allocate strings on every LazyColumn pass
                    val rowKeys = remember(rows) {
                        rows.map { row -> row.joinToString(",") { it.persistenceId } }
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
                            key = { rowIndex -> rowKeys[rowIndex] },
                        ) { rowIndex ->
                            val rowSections = rows[rowIndex]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
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
                                        gridColumns = gridColumns,
                                        heightUnits = dashConfig.gridSize.heightUnits,
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
                                            dragSection = section
                                            dragOffset = Offset.Zero
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
                                            dragSection = null
                                            dragOffset = Offset.Zero
                                            viewModel.saveDashboardLayout(draftOrder.toList(), dashConfig.hiddenIds)
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

}
