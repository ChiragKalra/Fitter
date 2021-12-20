package com.bruhascended.fitapp.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.db.activity.entities.DayEntry
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.dashboard.components.ConcentricCircles
import com.bruhascended.fitapp.ui.dashboard.components.OverViewCard
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.util.getWeekList


class DashboardFragment : Fragment() {
    private val outerCircleDiameter = 250.dp
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var repo: PreferencesRepository

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
                val defaultList = getWeekList(context)
                var energyExpLIst by remember {
                    mutableStateOf(defaultList)
                }
                var stepsLIst by remember {
                    mutableStateOf(defaultList)
                }

                var goalsData by remember {
                    mutableStateOf(DayEntry(0L))
                }

                viewModel.data?.observe(viewLifecycleOwner, {
                    energyExpLIst = viewModel.getLastWeekEnergyExp(it, energyExpLIst)
                    stepsLIst = viewModel.getLastWeekSteps(it, stepsLIst)
                    goalsData = if (it.isNotEmpty()) it.last() else DayEntry(0L)
                })

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        TopBar(intent)
                    }

                    item {
                        ConcentricCircles(
                            outerCircleDiameter,
                            goalsData,
                            activityGoals,
                            nutrientGoals
                        )
                    }

                    item {
                        OverViewCard(
                            stepsLIst,
                            context,
                            "Steps",
                            "steps",
                            activityGoals.steps
                        )
                    }

                    item {
                        OverViewCard(
                            energyExpLIst,
                            context,
                            "Energy burned",
                            "Cal",
                            activityGoals.calories
                        )
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
        return view
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
                    contentDescription = "settings"
                )
            }
        }
    }

}
