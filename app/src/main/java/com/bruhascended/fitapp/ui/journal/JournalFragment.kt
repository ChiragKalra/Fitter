package com.bruhascended.fitapp.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentJournalBinding
import com.bruhascended.fitapp.util.applyStatusBarPadding
import com.google.android.material.tabs.TabLayoutMediator


class JournalFragment : Fragment() {

    companion object {
        private const val TAB_STATE = "TAB_STATE"
    }

    private lateinit var binding: FragmentJournalBinding

    private fun setupViewPager() {
        binding.apply {
            viewPager.apply {
                adapter = ViewPagerAdaptor(
                    childFragmentManager, lifecycle
                )
                offscreenPageLimit = 2
            }
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = requireContext().getString(
                    if (position == 0) R.string.food else R.string.activity
                )
            }.attach()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentJournalBinding.inflate(inflater)

        // restore savedInstanceState
        binding.apply {
            tabLayout.selectTab(
                tabLayout.getTabAt(
                    savedInstanceState?.getInt(TAB_STATE) ?: 0
                )
            )
        }
        setupViewPager()
        binding.root.applyStatusBarPadding()

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(TAB_STATE, binding.tabLayout.selectedTabPosition)
        super.onSaveInstanceState(outState)
    }
}
