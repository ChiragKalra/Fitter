package com.bruhascended.fitapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentDashboardBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    private val viewModel: DashboardViewModel by viewModels()

    private fun setupAppbar() {
        //customise appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(true, true)
        val param: AppBarLayout.LayoutParams =
            activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.layoutParams
                    as AppBarLayout.LayoutParams
        param.scrollFlags = SCROLL_FLAG_SCROLL or
                SCROLL_FLAG_SNAP or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater)

        setupAppbar()

        return binding.root
    }

}
