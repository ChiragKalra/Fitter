package com.bruhascended.fitapp.ui.activityjournal

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
import com.bruhascended.fitapp.databinding.FragmentJournalActivityBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class ActivityJournalFragment: Fragment() {

    private val viewModel: ActivityJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalActivityBinding
    private lateinit var mAdaptor: ActivityJournalRecyclerAdapter

    private fun setupRecyclerView() {
        mAdaptor = ActivityJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItems
        )

        binding.recyclerviewActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
        }

        val dateSeparated = viewModel.activityEntries
            .map { pagingData ->
                pagingData.map {
                    DateSeparatedItem(DateSeparatedItem.ItemType.Item, item = it)
                }
            }
            .map { pagingData ->
                pagingData.insertSeparators{ after, before ->
                    val afterDate = after?.item?.date
                    val beforeDate = before?.item?.date
                    if (before == null) {
                        DateSeparatedItem(DateSeparatedItem.ItemType.Footer)
                    } else if (afterDate != null && afterDate <= beforeDate) {
                        null
                    } else {
                        DateSeparatedItem(
                            DateSeparatedItem.ItemType.Separator,
                            separator = beforeDate,
                            liveDayEntry = viewModel.separatorInfoOf(beforeDate!!)
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
        binding = FragmentJournalActivityBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }
}
