package com.bruhascended.fitapp.ui.activityjournal

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
import com.bruhascended.fitapp.databinding.FragmentJournalActivityBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class ActivityJournalFragment: Fragment() {

    private val viewModel: ActivityJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalActivityBinding
    private lateinit var mAdaptor: ActivityJournalRecyclerAdapter

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
        mAdaptor = ActivityJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItems,
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

        binding.recyclerviewActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
            addItemDecoration(FooterDecoration(footerHeight))
        }

        val dateSeparated = viewModel.activityEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map { pagingData ->
                pagingData.insertSeparators{ after, before ->
                    val afterDate = after?.item?.date
                    val beforeDate = before?.item?.date
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

        viewModel.lastItems.observe(viewLifecycleOwner) {
            binding.recyclerviewActivities.invalidateItemDecorations()
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