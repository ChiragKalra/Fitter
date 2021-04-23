package com.bruhascended.fitapp.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.databinding.FragmentDashboardBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout


class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater)

        //customise appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(true, true)
        val param: AppBarLayout.LayoutParams = activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.layoutParams as AppBarLayout.LayoutParams
        param.scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_SNAP or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

        return binding.root
    }


}