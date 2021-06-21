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
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class FoodJournalFragment: Fragment() {

    private val viewModel: FoodJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalFoodBinding
    private lateinit var mAdaptor: FoodJournalRecyclerAdapter

    private fun setupRecyclerView() {
        mAdaptor = FoodJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItemLiveSet,
            viewModel.separatorInfoMap,
        ).apply {
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

        viewModel.liveFoodEntries.observe(viewLifecycleOwner) { all ->
            val infoMap = HashMap<Date, FoodJournalViewModel.SeparatorInfo>()
            all.forEach {
                val date = it.entry.date
                if (!infoMap.containsKey(date)) {
                    infoMap[date] = FoodJournalViewModel.SeparatorInfo()
                }
                infoMap[date]?.apply {
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
            viewModel.separatorInfoMap.postValue(infoMap)

            val allArr = all.toTypedArray()
            val newIdSet = HashSet<Long>().apply {
                if (allArr.isNotEmpty()) {
                    add(allArr.last().entry.entryId!!)
                }
                if (allArr.size > 1) {
                    allArr.slice( 0 until all.size - 1).forEachIndexed { ind, foodEntry ->
                        if (foodEntry.entry.date != allArr[ind+1].entry.date) {
                            add(foodEntry.entry.entryId!!)
                        }
                    }
                }
            }
            viewModel.lastItemLiveSet.postValue(newIdSet)
        }

        val dateSeparated = viewModel.foodEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map {
                it.insertSeparators{ after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    if (beforeDate == null || (afterDate != null && afterDate <= beforeDate)) {
                        null
                    } else {
                        DateSeparatedItem(
                            separator = beforeDate,
                        )
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