package com.bruhascended.fitapp.ui.challenges

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.FragmentChallengesBinding
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

        return binding.root
    }
}
