package com.bruhascended.fitapp.challenges

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.databinding.FragmentChallengesBinding
import com.bruhascended.fitapp.main.MainActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

class ChallengesFragment : Fragment() {
    private lateinit var binding: FragmentChallengesBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChallengesBinding.inflate(inflater)

        //customise appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(true, true)
        val param: AppBarLayout.LayoutParams = activity?.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)?.layoutParams as AppBarLayout.LayoutParams
        param.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED

        return binding.root
    }
}