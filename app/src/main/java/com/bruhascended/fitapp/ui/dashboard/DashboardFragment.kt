package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.NutrientCard
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.ui.theme.Blue500
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import com.bruhascended.fitapp.ui.theme.Green200
import com.bruhascended.fitapp.ui.theme.Red200
import com.bruhascended.fitapp.util.getWeekList
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import kotlin.math.ceil
import kotlin.math.floor

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
                FitAppTheme() {
                    val defaultList = getWeekList(context)
                    var energyExpList by remember { mutableStateOf(defaultList) }
                    var stepsList by remember { mutableStateOf(defaultList) }

                    var todayActivityData by remember {
                        mutableStateOf(DayEntry(0L))
                    }

                    var todayNutrientData by remember {
                        mutableStateOf(com.bruhascended.db.food.entities.DayEntry(0L))
                    }

                    val lifecycleOwner = LocalLifecycleOwner.current
                    val actLd = viewModel.activityData
                    val nutLd = viewModel.nutrientData

                    DisposableEffect(actLd, nutLd, lifecycleOwner) {
                        val activityObserver = Observer<List<DayEntry>> { list ->
                            energyExpList = viewModel.getLastWeekEnergyExp(list, energyExpList)
                            stepsList = viewModel.getLastWeekSteps(list, stepsList)
                            todayActivityData = if (list.isNotEmpty()) list.last() else DayEntry(0L)
                        }
                        val nutrientObserver =
                            Observer<com.bruhascended.db.food.entities.DayEntry?> { entry ->
                                todayNutrientData = entry ?: viewModel.defaultNutrientData
                            }
                        actLd?.observe(lifecycleOwner, activityObserver)
                        nutLd?.observe(lifecycleOwner, nutrientObserver)
                        onDispose {
                            actLd?.removeObserver(activityObserver)
                            nutLd?.removeObserver(nutrientObserver)
                        }
                    }

                    val dashConfig by produceState(DashboardUiConfig.Default, viewModel) {
                        viewModel.dashboardUiConfig.collect { value = it }
                    }

                    var layoutEditMode by remember { mutableStateOf(false) }
                    var draftOrder by remember { mutableStateOf(DashboardSection.defaultOrdered) }
                    var draftHiddenIds by remember { mutableStateOf(emptySet<DashboardSection>()) }

                    LaunchedEffect(dashConfig, layoutEditMode) {
                        if (!layoutEditMode) {
                            draftOrder = dashConfig.order
                            draftHiddenIds = dashConfig.hiddenIds
                        }
                    }

                    BackHandler(enabled = layoutEditMode) {
                        layoutEditMode = false
                    }

                    val visibleSections =
                        if (layoutEditMode) draftOrder.filter { it !in draftHiddenIds }
                        else dashConfig.visibleOrdered()

                    val hiddenOrdered =
                        if (layoutEditMode) {
                            draftOrder.filter { it in draftHiddenIds }
                        } else {
                            emptyList()
                        }

                    val listBottomPadding =
                        dimensionResource(R.dimen.main_bottom_nav_height) + 160.dp

                    val reorderState = rememberReorderableLazyListState(
                        onMove = { from, to ->
                            if (!layoutEditMode) return@rememberReorderableLazyListState
                            val fromKey = from.key as? String ?: return@rememberReorderableLazyListState
                            val toKey = to.key as? String ?: return@rememberReorderableLazyListState
                            val orderedVisible =
                                draftOrder.filter { it !in draftHiddenIds }
                            val fromVisible =
                                orderedVisible.indexOfFirst { it.persistenceId == fromKey }
                            val toVisible =
                                orderedVisible.indexOfFirst { it.persistenceId == toKey }
                            if (fromVisible < 0 || toVisible < 0) {
                                return@rememberReorderableLazyListState
                            }
                            draftOrder = reorderVisibleInFullOrder(
                                draftOrder,
                                draftHiddenIds,
                                fromVisible,
                                toVisible,
                            )
                            viewModel.saveDashboardLayout(draftOrder, draftHiddenIds)
                        },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .background(MaterialTheme.colors.background),
                    ) {
                        DashboardTopBar(
                            layoutEditMode = layoutEditMode,
                            onExitEditMode = { layoutEditMode = false },
                            onOpenSettings = { startActivity(intent) },
                            modifier = Modifier.fillMaxWidth(),
                        )

                        LazyColumn(
                            state = reorderState.listState,
                            contentPadding = PaddingValues(bottom = listBottomPadding),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true)
                                .reorderable(reorderState)
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(if (layoutEditMode) 10.dp else 18.dp),
                        ) {
                            if (layoutEditMode && visibleSections.isEmpty()) {
                                item(key = "__empty_dashboard__") {
                                    Text(
                                        text = stringResource(R.string.dashboard_all_hidden_placeholder),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 32.dp, bottom = 12.dp),
                                    )
                                }
                            }

                            items(visibleSections, key = { it.persistenceId }) { section ->
                                ReorderableItem(
                                    reorderState,
                                    key = section.persistenceId,
                                ) { isDragging ->
                                    val dragElevation by animateDpAsState(
                                        if (isDragging) 12.dp else 0.dp,
                                        label = "cardDragElev",
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        if (layoutEditMode) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_drag_handle),
                                                contentDescription =
                                                    stringResource(R.string.dashboard_drag_handle_a11y),
                                                tint =
                                                    MaterialTheme.colors.onSurface.copy(alpha = 0.55f),
                                                modifier = Modifier
                                                    .detectReorder(reorderState)
                                                    .padding(end = 4.dp)
                                                    .size(32.dp),
                                            )
                                        }

                                        val cardOutlineColor =
                                            MaterialTheme.colors.primary.copy(alpha = 0.45f)

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .shadow(
                                                    elevation = dragElevation,
                                                    shape = RoundedCornerShape(14.dp),
                                                    clip = false,
                                                )
                                                .then(
                                                    if (layoutEditMode) {
                                                        Modifier.border(
                                                            BorderStroke(
                                                                width = (if (isDragging) 1.8f else 1f).dp,
                                                                color =
                                                                    if (isDragging)
                                                                        MaterialTheme.colors.primary
                                                                    else
                                                                        cardOutlineColor,
                                                            ),
                                                            shape = RoundedCornerShape(14.dp),
                                                        )
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .pointerInput(layoutEditMode) {
                                                    detectTapGestures(
                                                        onLongPress = {
                                                            if (!layoutEditMode) {
                                                                draftOrder = dashConfig.order
                                                                draftHiddenIds = dashConfig.hiddenIds
                                                                layoutEditMode = true
                                                            }
                                                        },
                                                    )
                                                },
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
                                                context = context,
                                            )
                                        }

                                        if (layoutEditMode) {
                                            HideGlyphButton(
                                                onClick = {
                                                    draftHiddenIds = draftHiddenIds + section
                                                    viewModel.saveDashboardLayout(
                                                        draftOrder,
                                                        draftHiddenIds,
                                                    )
                                                },
                                                contentDescription =
                                                    stringResource(R.string.dashboard_hide_card_a11y),
                                            )
                                        }
                                    }
                                }
                            }

                            if (layoutEditMode && hiddenOrdered.isNotEmpty()) {
                                item(key = "__dash_hidden_separator__") {
                                    Divider(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                        thickness = 1.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 18.dp),
                                    )
                                }

                                items(
                                    hiddenOrdered,
                                    key = { "${it.persistenceId}_hidden_rest" },
                                ) { section ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Spacer(
                                            modifier = Modifier
                                                .width(36.dp),
                                        )

                                        val cardOutlineColor =
                                            MaterialTheme.colors.onSurface.copy(alpha = 0.25f)

                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(
                                                    BorderStroke(1.dp, cardOutlineColor),
                                                    shape = RoundedCornerShape(14.dp),
                                                ),
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
                                                context = context,
                                            )
                                        }

                                        RestoreGlyphButton(
                                            onClick = {
                                                draftHiddenIds = draftHiddenIds - section
                                                viewModel.saveDashboardLayout(
                                                    draftOrder,
                                                    draftHiddenIds,
                                                )
                                            },
                                            contentDescription =
                                                stringResource(section.titleRes) + " · " +
                                                    stringResource(R.string.dashboard_show_card_a11y),
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

    @Composable
    private fun HideGlyphButton(
        onClick: () -> Unit,
        contentDescription: String,
        modifier: Modifier = Modifier,
    ) {
        val outline = MaterialTheme.colors.onSurface.copy(alpha = 0.28f)
        Box(
            modifier = modifier
                .padding(start = 6.dp)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, outline, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "\u2715",
                fontSize = 18.sp,
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    @Composable
    private fun RestoreGlyphButton(
        onClick: () -> Unit,
        contentDescription: String,
        modifier: Modifier = Modifier,
    ) {
        val outline = MaterialTheme.colors.primary.copy(alpha = 0.45f)
        Box(
            modifier = modifier
                .padding(start = 6.dp)
                .size(40.dp)
                .clip(CircleShape)
                .border(1.dp, outline, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_done),
                tint = MaterialTheme.colors.primary,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }

    @Composable
    private fun DashboardTopBar(
        layoutEditMode: Boolean,
        onExitEditMode: () -> Unit,
        onOpenSettings: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        if (layoutEditMode) {
            Surface(
                color = MaterialTheme.colors.surface.copy(alpha = 0.97f),
                elevation = 2.dp,
                modifier = modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_edit_banner),
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onExitEditMode) {
                        Text(stringResource(R.string.dashboard_edit_done))
                    }
                }
            }
        } else {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        tint = MaterialTheme.colors.onSurface,
                        contentDescription = stringResource(R.string.settings),
                    )
                }
            }
        }
    }

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
        context: android.content.Context,
    ) {
        when (section) {
            DashboardSection.SUMMARY_RING -> ConcentricCircles(
                outerCircleDiameter,
                todayActivityData,
                todayNutrientData,
                activityGoals,
                nutrientGoals,
            )

            DashboardSection.TODAY_STATS -> CurrentDayStats(todayActivityData, todayNutrientData)

            DashboardSection.STEPS_WEEK -> OverViewCard(
                stepsList,
                context,
                "Steps",
                "steps",
                activityGoals.steps,
                Blue500,
            )

            DashboardSection.ENERGY_WEEK -> OverViewCard(
                energyExpList,
                context,
                "Energy burned",
                "Cal",
                activityGoals.calories,
                Red200,
            )

            DashboardSection.NUTRIENTS -> NutrientCard(todayNutrientData)
        }
    }

    @Composable
    private fun CurrentDayStats(
        todayData: DayEntry,
        todayNutrientData: com.bruhascended.db.food.entities.DayEntry,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CurrentDayItem(
                    "Cal",
                    stringFormatter(todayData.totalCalories.toInt()),
                    painterResource(id = R.drawable.ic_energy_burn),
                    Red200,
                    "Energy Burned",
                )
                CurrentDayItem(
                    "",
                    stringFormatter(todayData.totalSteps),
                    painterResource(id = R.drawable.ic_steps),
                    Blue500,
                    "Steps",
                )
                CurrentDayItem(
                    "Cal",
                    stringFormatter(todayNutrientData.calories),
                    painterResource(id = R.drawable.ic_consumed),
                    Green200,
                    "Energy Consumed",
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CurrentDayItem(
                    "km",
                    stringFormatter(todayData.totalDistance.toFloat()),
                    painterResource(id = R.drawable.ic_distance),
                    MaterialTheme.colors.onSurface,
                    "Distance",
                )
                CurrentDayItem(
                    "min",
                    stringFormatter(todayData.totalDuration / 60000f),
                    painterResource(id = R.drawable.ic_duration),
                    MaterialTheme.colors.onSurface,
                    "Duration",
                )
            }
        }
    }

    private fun stringFormatter(data: Any): String {
        val str = String.format("%.1f", data.toString().toFloat())
        val num = str.toFloat()
        return if (ceil(num) == floor(num)) num.toInt().toString()
        else str
    }

    @Composable
    fun CurrentDayItem(
        unit: String = "",
        data: String,
        painter: Painter,
        color: Color,
        description: String,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painter,
                tint = color,
                contentDescription = description,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 4.dp),
            )
            Text(
                text = "$data $unit",
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

}
