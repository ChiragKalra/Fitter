package com.bruhascended.fitapp.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bruhascended.db.food.entities.DayEntry
import com.bruhascended.db.food.types.NutrientType

import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout

import com.bruhascended.fitapp.R

import com.bruhascended.fitapp.databinding.FragmentDashboardBinding
import java.util.*


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()


    private lateinit var cardGens: List<WeeklyPlotPresenter>

    private fun addCards() {
        // declare card generators
        val nutritionCardGen = NutritionCardPresenter(
            requireContext(),
            layoutInflater
        )
        val activityCardGen = ActivityCardPresenter(
            requireContext(),
            layoutInflater
        )

        cardGens = WeeklyCardType.values().map {
            WeeklyPlotPresenter(
                requireContext(),
                layoutInflater,
                it
            )
        }

        // add cards to root
        binding.contentLayout.addView(nutritionCardGen.view)
        binding.contentLayout.addView(activityCardGen.view)

        for (cardGen in cardGens) {
            binding.contentLayout.addView(cardGen.view)
        }

        // fill cards with data
        viewModel.getLastWeekDayEntries().observeForever {
            for (cardGen in cardGens) {
                val pro = arrayListOf<DayEntry>()
                val weekBefore = Calendar.getInstance().apply {
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.HOUR_OF_DAY, 0)
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                for (i in 0..6) {
                    val day = weekBefore.apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }.timeInMillis
                    val got = it.firstOrNull { entry ->
                        entry.day == day
                    } ?: DayEntry(day)
                    pro.add(got)
                }
                if (cardGen.plotType in arrayOf(
                        WeeklyCardType.WeeklyFatsConsumed,
                        WeeklyCardType.WeeklyProteinsConsumed,
                        WeeklyCardType.WeeklyCarbsConsumed,
                        WeeklyCardType.WeeklyCaloriesConsumed,
                )) {
                    cardGen.generateCard(
                        pro.map { entry ->
                            when (cardGen.plotType) {
                                WeeklyCardType.WeeklyCaloriesConsumed ->
                                    entry.calories.toFloat()
                                WeeklyCardType.WeeklyProteinsConsumed ->
                                    entry.nutrientInfo[NutrientType.Protein]?.toFloat() ?: 0f
                                WeeklyCardType.WeeklyCarbsConsumed ->
                                    entry.nutrientInfo[NutrientType.Carbs]?.toFloat() ?: 0f
                                else ->
                                    entry.nutrientInfo[NutrientType.Fat]?.toFloat() ?: 0f
                            }
                        }.toFloatArray()
                    )
                }
            }
        }

        viewModel.getLastWeekActivityEntries().observeForever {
            val pro = it.toTypedArray()
            for (cardGen in cardGens) {
                if (cardGen.plotType in arrayOf(
                        WeeklyCardType.WeeklyCaloriesBurnt,
                        WeeklyCardType.WeeklyDistanceCovered,
                        WeeklyCardType.WeeklyStepsTaken,
                        WeeklyCardType.WeeklyActiveTime,
                )) {
                    cardGen.generateCard(
                        pro.map { entry ->
                            when (cardGen.plotType) {
                                WeeklyCardType.WeeklyCaloriesBurnt ->
                                    entry.totalCalories
                                WeeklyCardType.WeeklyDistanceCovered ->
                                    entry.totalDistance.toFloat()
                                WeeklyCardType.WeeklyStepsTaken ->
                                    entry.totalSteps.toFloat()
                                else ->
                                    entry.totalDuration / (60 * 1000f)
                            }
                        }.toFloatArray()
                    )
                }
            }
        }

        viewModel.getTodayLiveNutrition().observeForever {
            nutritionCardGen.generateCard(it ?: return@observeForever)
        }
        viewModel.getTodayLiveActivity().observeForever {
            activityCardGen.generateCard(it ?: return@observeForever)
        }
    }

    private fun setupAppbar() {
        //customise appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(true, true)
        val param: AppBarLayout.LayoutParams =
            activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.layoutParams
                    as AppBarLayout.LayoutParams
        param.scrollFlags = SCROLL_FLAG_SCROLL or
                SCROLL_FLAG_SNAP or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater)

        setupAppbar()
        addCards()

        return binding.root
    }

}
