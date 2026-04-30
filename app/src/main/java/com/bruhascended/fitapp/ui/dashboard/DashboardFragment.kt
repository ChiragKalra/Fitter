package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.NutrientCard
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.ui.theme.*
import com.bruhascended.fitapp.util.getWeekList
import kotlin.math.ceil
import kotlin.math.floor


class DashboardFragment : Fragment() {
    private val outerCircleDiameter = 200.dp
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
                    var energyExpLIst by remember {
                        mutableStateOf(defaultList)
                    }
                    var stepsLIst by remember {
                        mutableStateOf(defaultList)
                    }

                    var todayActivityData by remember {
                        mutableStateOf(DayEntry(0L))
                    }

                    var todayNutrientData by remember {
                        mutableStateOf(com.bruhascended.db.food.entities.DayEntry(0L))
                    }


                    viewModel.apply {
                        activityData?.observe(viewLifecycleOwner, {
                            energyExpLIst = viewModel.getLastWeekEnergyExp(it, energyExpLIst)
                            stepsLIst = viewModel.getLastWeekSteps(it, stepsLIst)
                            todayActivityData = if (it.isNotEmpty()) it.last() else DayEntry(0L)
                        })
                        nutrientData?.observe(viewLifecycleOwner) {
                            todayNutrientData = it ?: viewModel.defaultNutrientData
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .background(MaterialTheme.colors.background),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item {
                            TopBar(intent)
                        }

                        item {
                            ConcentricCircles(
                                outerCircleDiameter,
                                todayActivityData,
                                todayNutrientData,
                                activityGoals,
                                nutrientGoals
                            )
                        }

                        item {
                            CurrentDayStats(todayActivityData, todayNutrientData)
                        }

                        item {
                            OverViewCard(
                                stepsLIst,
                                context,
                                "Steps",
                                "steps",
                                activityGoals.steps,
                                Blue500
                            )
                        }

                        item {
                            OverViewCard(
                                energyExpLIst,
                                context,
                                "Energy burned",
                                "Cal",
                                activityGoals.calories,
                                Red200
                            )
                        }

                        item {
                            NutrientCard(todayNutrientData)
                        }

                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }
        }
        return view
    }

    @Composable
    private fun CurrentDayStats(
        todayData: DayEntry,
        todayNutrientData: com.bruhascended.db.food.entities.DayEntry
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CurrentDayItem(
                    "Cal",
                    stringFormatter(todayData.totalCalories.toInt()),
                    painterResource(id = R.drawable.ic_energy_burn),
                    Red200,
                    "Energy Burned"
                )
                CurrentDayItem(
                    "",
                    stringFormatter(todayData.totalSteps),
                    painterResource(id = R.drawable.ic_steps),
                    Blue500,
                    "Steps"
                )
                CurrentDayItem(
                    "Cal",
                    stringFormatter(todayNutrientData.calories),
                    painterResource(id = R.drawable.ic_consumed),
                    Green200,
                    "Energy Consumed"
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CurrentDayItem(
                    "km",
                    stringFormatter(todayData.totalDistance.toFloat()),
                    painterResource(id = R.drawable.ic_distance),
                    MaterialTheme.colors.onSurface,
                    "Distance"
                )
                CurrentDayItem(
                    "min",
                    stringFormatter(todayData.totalDuration / 60000f),
                    painterResource(id = R.drawable.ic_duration),
                    MaterialTheme.colors.onSurface,
                    "Duration"
                )
            }
        }
    }

    private fun stringFormatter(data: Any): String {
        val str = String.format("%.1f", data.toString().toFloat())
        val data = str.toFloat()
        return if (ceil(data) == floor(data)) data.toInt().toString()
        else str
    }

    @Composable
    fun CurrentDayItem(
        unit: String = "",
        data: String,
        painter: Painter,
        color: Color,
        description: String
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painter,
                tint = color,
                contentDescription = description,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                text = "$data $unit",
                color = MaterialTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    @Composable
    private fun TopBar(intent: Intent) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = {
                startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    tint = MaterialTheme.colors.onSurface,
                    contentDescription = "settings"
                )
            }
        }
    }

}
