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


class FoodJournalFragment: Fragment() {

    private val viewModel: FoodJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalFoodBinding
    private lateinit var mAdaptor: FoodJournalRecyclerAdapter

    private fun setupRecyclerView() {
        mAdaptor = FoodJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItemIds
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

        val dateSeparated = viewModel.foodEntries
            .map { pagingData ->
                pagingData.map {
                    DateSeparatedItem(
                        DateSeparatedItem.ItemType.Item,
                        item = it
                    )
                }
            }
            .map { pagingData ->
                pagingData.insertSeparators { after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    if (before == null) {
                        DateSeparatedItem(DateSeparatedItem.ItemType.Footer)
                    } else if (
                        beforeDate == null || (afterDate != null && afterDate <= beforeDate)
                    ) {
                        null
                    } else {
                        DateSeparatedItem(
                            DateSeparatedItem.ItemType.Separator,
                            separator = beforeDate,
                            liveDayEntry = viewModel.getSeparatorInfo(beforeDate)
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
