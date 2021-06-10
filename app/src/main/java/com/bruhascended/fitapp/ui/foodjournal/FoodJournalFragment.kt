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
import com.bruhascended.fitapp.util.datetime.DateSeparatedItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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

        val dateSeparated = viewModel.foodEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map {
                it.insertSeparators{ after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    when {
                        beforeDate == null ->
                            null
                        afterDate == null ->
                            DateSeparatedItem(separator = beforeDate)
                        afterDate > beforeDate ->
                            DateSeparatedItem(separator = beforeDate)
                        else ->
                            null
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