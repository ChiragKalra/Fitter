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
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class ActivityJournalFragment: Fragment() {

    private val viewModel: ActivityJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalActivityBinding
    private lateinit var mAdaptor: ActivityJournalRecyclerAdapter

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

        binding.recyclerviewActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
            addItemDecoration(
                FooterDecoration(
                    requireContext().resources
                        .getDimension(R.dimen.footer_height).toInt()
                )
            )
        }

        viewModel.liveActivityEntries.observe(viewLifecycleOwner) { all ->
            val infoMap = HashMap<Date, ActivityJournalViewModel.SeparatorInfo>()
            all.forEach {
                val date = it.date
                if (!infoMap.containsKey(date)) {
                    infoMap[date] = ActivityJournalViewModel.SeparatorInfo()
                }
                infoMap[date]?.also { info -> info += it }
            }
            viewModel.separatorInfoMap.postValue(infoMap)

            val allArr = all.toTypedArray()
            val newIdSet = HashSet<Long>().apply {
                if (allArr.isNotEmpty()) {
                    add(allArr.last().id)
                }
                if (allArr.size > 1) {
                    allArr.slice( 0 until all.size - 1).forEachIndexed { ind, activityEntry ->
                        if (activityEntry.date != allArr[ind+1].date) {
                            add(activityEntry.id)
                        }
                    }
                }
            }
            viewModel.lastItemLiveSet.postValue(newIdSet)
            binding.recyclerviewActivities.invalidateItemDecorations()
        }

        val dateSeparated = viewModel.activityEntries
            .map { pagingData -> pagingData.map { DateSeparatedItem(item = it) } }
            .map {
                it.insertSeparators{ after, before ->
                    val afterDate = after?.item?.date
                    val beforeDate = before?.item?.date
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
        binding = FragmentJournalActivityBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }
}