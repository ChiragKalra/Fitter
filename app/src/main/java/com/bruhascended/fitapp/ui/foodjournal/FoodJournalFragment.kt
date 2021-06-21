package com.bruhascended.fitapp.ui.foodjournal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruhascended.fitapp.databinding.FragmentJournalFoodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashSet


class FoodJournalFragment: Fragment() {

    private val viewModel: FoodJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalFoodBinding
    private lateinit var mAdaptor: FoodJournalRecyclerAdapter
    private val mSeparatorMap = hashMapOf<Date, DateSeparatedItem>()

    private fun setupRecyclerView() {
        mAdaptor = FoodJournalRecyclerAdapter(requireContext()).apply {
            setOnItemClickListener {
                ActionDialogPresenter(
                    requireContext(),
                    viewModel,
                    it
                ).show()
            }
        }

        binding.recyclerviewFoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
        }

        val resetDays = HashSet<Date>()
        viewModel.liveFoodEntries.observe(viewLifecycleOwner) { all ->
            resetDays.forEach {
                mSeparatorMap[it]?.apply {
                    totalCalories = 0
                    totalNutrients.keys.forEach { nutrient ->
                        totalNutrients[nutrient] = .0
                    }
                }
            }
            resetDays.clear()
            all.forEach {
                val date = it.entry.date
                resetDays.add(date)
                if (!mSeparatorMap.containsKey(date)) {
                    mSeparatorMap[date] = DateSeparatedItem(
                        separator = date,
                    )
                }
                mSeparatorMap[date]?.apply {
                    totalCalories += it.entry.calories
                    val amountPerQuantity = it.food.weightInfo[it.entry.quantityType]
                    if (amountPerQuantity != null) {
                        val amount = it.entry.quantity * amountPerQuantity
                        it.food.nutrientInfo.forEach { (key, value) ->
                            totalNutrients[key] = (totalNutrients[key] ?: .0) + value * amount
                        }
                    }
                }
            }
            mAdaptor.notifyDataSetChanged()
        }

        val dateSeparated = viewModel.foodEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map {
                it.insertSeparators{ after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    if (beforeDate == null) {
                        null
                    } else {
                        if (!mSeparatorMap.containsKey(beforeDate)) {
                            mSeparatorMap[beforeDate] = DateSeparatedItem(
                                separator = beforeDate,
                            )
                        }
                        if (afterDate == null || afterDate > beforeDate) {
                            mSeparatorMap[beforeDate]
                        } else {
                            null
                        }
                    }
                }
            }

        lifecycleScope.launch {
            dateSeparated.cachedIn(lifecycleScope).collectLatest {
                mAdaptor.submitData(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentJournalFoodBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }
}