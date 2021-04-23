package com.bruhascended.fitapp.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentJournalBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL as SCROLL_FLAG_SCROLL1


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
            nestedScrollView.scrollY = savedInstanceState?.getInt(SCROLL_STATE) ?: 0
        }

        //customise the appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(true, true)
        val param: AppBarLayout.LayoutParams = activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.layoutParams as AppBarLayout.LayoutParams
        param.scrollFlags = SCROLL_FLAG_SNAP or SCROLL_FLAG_SCROLL1 or SCROLL_FLAG_ENTER_ALWAYS or SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SCROLL_STATE, binding.nestedScrollView.scrollY)
        outState.putInt(TAB_STATE, binding.tabLayout.selectedTabPosition)
        super.onSaveInstanceState(outState)
    }
}