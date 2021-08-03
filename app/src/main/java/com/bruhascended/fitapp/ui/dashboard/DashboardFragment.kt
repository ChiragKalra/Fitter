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
        cardGens = WeeklyCardType.values().map {
            WeeklyPlotPresenter(
                requireContext(),
                layoutInflater,
                it
            )
        }

        for (cardGen in cardGens) {
            binding.contentLayout.addView(cardGen.view)
        }

        for (cardGen in cardGens) {
            viewModel.getLastWeekDayEntries().observeForever {
                val pro = arrayListOf<DayEntry>()
                val weekBefore = Calendar.getInstance().apply {
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.HOUR_OF_DAY, 0)
                    add(Calendar.DAY_OF_YEAR, -7)
                }
                val weekDayInt = weekBefore.get(Calendar.DAY_OF_WEEK)-1
                weekBefore.add(Calendar.DAY_OF_YEAR, 6-weekDayInt)
                for (i in 0..weekDayInt) {
                    val day = weekBefore.apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }.timeInMillis
                    val got = it.firstOrNull { entry ->
                        entry.day == day
                    } ?: DayEntry(day)
                    pro.add(got)
                }
                cardGen.generateChart(
                    pro.map { entry ->
                        when (cardGen.plotType) {
                            WeeklyCardType.WeeklyCaloriesConsumed ->
                                entry.calories.toFloat()
                            WeeklyCardType.WeeklyProteinsConsumed ->
                                entry.nutrientInfo[NutrientType.Protein]?.toFloat() ?: 0f
                            WeeklyCardType.WeeklyCarbsConsumed ->
                                entry.nutrientInfo[NutrientType.Carbs]?.toFloat() ?: 0f
                            WeeklyCardType.WeeklyFatsConsumed ->
                                entry.nutrientInfo[NutrientType.Fat]?.toFloat() ?: 0f
                        }
                    }.toFloatArray()
                )
            }
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
