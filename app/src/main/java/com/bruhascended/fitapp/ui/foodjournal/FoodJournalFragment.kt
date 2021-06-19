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

class FoodJournalFragment: Fragment() {

    private val viewModel: FoodJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalFoodBinding
    private lateinit var mAdaptor: FoodJournalRecyclerAdapter

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

        // TODO fix on item delete updates don't show
        val separatorMap = hashMapOf<Date, DateSeparatedItem>()

        val dateSeparated = viewModel.foodEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map {
                it.insertSeparators{ after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    if (beforeDate == null) {
                        null
                    } else {
                        if (!separatorMap.containsKey(beforeDate)) {
                            separatorMap[beforeDate] = DateSeparatedItem(
                                separator = beforeDate,
                            )
                        }
                        if (before.item.food.weightInfo.containsKey(before.item.entry.quantityType)) {
                            separatorMap[beforeDate]?.apply {
                                totalCalories += before.item.entry.calories
                                val weight =
                                    before.item.entry.quantity *
                                            (before.item.food.weightInfo[before.item.entry.quantityType] ?: .0)
                                before.item.food.nutrientInfo.forEach { (key, item) ->
                                    totalNutrients[key] = item*weight+ (totalNutrients[key] ?: 0.0)
                                }
                            }
                        }
                        if (afterDate == null || afterDate > beforeDate) {
                            separatorMap[beforeDate]
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