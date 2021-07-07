package com.bruhascended.fitapp.ui.foodjournal

import android.graphics.Rect
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
import androidx.recyclerview.widget.RecyclerView
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentJournalFoodBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class FoodJournalFragment: Fragment() {

    private val viewModel: FoodJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalFoodBinding
    private lateinit var mAdaptor: FoodJournalRecyclerAdapter

    private val footerHeight: Int
    get() = requireContext().resources
            .getDimension(R.dimen.footer_height).toInt()

    class FooterDecoration(private val footerHeight: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val adapter = parent.adapter ?: return
            when (parent.getChildAdapterPosition(view)) {
                adapter.itemCount - 1 ->
                    outRect.bottom = footerHeight
                else ->
                    outRect.set(0, 0, 0, 0)
            }
        }
    }

    private fun setupRecyclerView() {
        mAdaptor = FoodJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItemIds,
            viewModel.separatorInfo,
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
            addItemDecoration(FooterDecoration(footerHeight))
        }

        val dateSeparated = viewModel.foodEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map { pagingData ->
                pagingData.insertSeparators { after, before ->
                    val afterDate = after?.item?.entry?.date
                    val beforeDate = before?.item?.entry?.date
                    if (beforeDate == null || (afterDate != null && afterDate <= beforeDate)) {
                        null
                    } else {
                        DateSeparatedItem(separator = beforeDate)
                    }
                }
            }

        lifecycleScope.launch {
            dateSeparated.cachedIn(lifecycleScope).collectLatest {
                mAdaptor.submitData(it)
            }
        }

        viewModel.lastItemIds.observe(viewLifecycleOwner) {
            binding.recyclerviewFoods.invalidateItemDecorations()
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
