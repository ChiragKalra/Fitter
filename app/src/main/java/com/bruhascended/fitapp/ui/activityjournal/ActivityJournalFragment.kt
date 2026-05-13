package com.bruhascended.fitapp.ui.activityjournal

import android.os.Bundle
import android.util.Log
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
import com.bruhascended.fitapp.databinding.FragmentJournalActivityBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class ActivityJournalFragment: Fragment() {

    private val viewModel: ActivityJournalViewModel by viewModels()

    private lateinit var binding: FragmentJournalActivityBinding
    private lateinit var mAdaptor: ActivityJournalRecyclerAdapter

    private fun setupRecyclerView() {
        Log.i(TAG, "setupRecyclerView")
        mAdaptor = ActivityJournalRecyclerAdapter(
            requireContext(),
            viewModel.lastItems
        )
        mAdaptor.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                Log.i(TAG, "adapter onChanged itemCount=${mAdaptor.itemCount}")
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                Log.i(TAG, "adapter inserted start=$positionStart count=$itemCount total=${mAdaptor.itemCount}")
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                Log.i(TAG, "adapter removed start=$positionStart count=$itemCount total=${mAdaptor.itemCount}")
            }
        })

        binding.recyclerviewActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
        }

        lifecycleScope.launch {
            viewModel.activityEntries.collectLatest {
                Log.i(TAG, "submitting activity-entry paging data")
                mAdaptor.submitData(it)
            }
        }

        lifecycleScope.launch {
            mAdaptor.loadStateFlow.collectLatest { loadStates ->
                Log.i(
                    TAG,
                    "loadState refresh=${loadStates.refresh::class.simpleName} " +
                        "append=${loadStates.append::class.simpleName} itemCount=${mAdaptor.itemCount}"
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "onCreateView")
        binding = FragmentJournalActivityBinding.inflate(inflater, container, false)

        setupRecyclerView()

        return binding.root
    }

    companion object {
        private const val TAG = "ActivityJournal"
    }
}
