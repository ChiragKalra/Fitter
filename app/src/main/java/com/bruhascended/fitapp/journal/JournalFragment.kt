package com.bruhascended.fitapp.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.databinding.FragmentJournalBinding
import com.bruhascended.fitapp.main.MainActivity
import com.google.android.material.appbar.AppBarLayout


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

        //expand the appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(false,true)

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SCROLL_STATE, binding.nestedScrollView.scrollY)
        outState.putInt(TAB_STATE, binding.tabLayout.selectedTabPosition)
        super.onSaveInstanceState(outState)
    }
}