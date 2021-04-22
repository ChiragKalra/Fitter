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

class ChallengesFragment : Fragment() {
    private lateinit var binding:FragmentChallengesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChallengesBinding.inflate(inflater)

        //expand the appbar
        val view = activity?.findViewById<AppBarLayout>(R.id.app_bar)
        view?.setExpanded(false,true)

        return binding.root
    }
}