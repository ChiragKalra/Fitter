package com.bruhascended.fitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.databinding.FragmentJournalBinding


class JournalFragment : Fragment() {

    companion object {
        private const val TAB_STATE = "TAB_STATE"
        private const val SCROLL_STATE = "SCROLL_STATE"
    }

    private lateinit var binding: FragmentJournalBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentJournalBinding.inflate(inflater)
        binding.apply {
            tabLayout.selectTab(
                tabLayout.getTabAt(
                    savedInstanceState?.getInt(TAB_STATE) ?: 0
                )
            )
            nestedScrollView.isNestedScrollingEnabled = false
            nestedScrollView.scrollY = savedInstanceState?.getInt(SCROLL_STATE) ?: 0
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SCROLL_STATE, binding.nestedScrollView.scrollY)
        outState.putInt(TAB_STATE, binding.tabLayout.selectedTabPosition)
        super.onSaveInstanceState(outState)
    }
}