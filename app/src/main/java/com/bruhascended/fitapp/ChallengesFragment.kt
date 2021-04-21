package com.bruhascended.fitapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruhascended.fitapp.databinding.FragmentChallengesBinding

class ChallengesFragment : Fragment() {
    private lateinit var binding:FragmentChallengesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentChallengesBinding.inflate(inflater)
        binding.nestedScrollView.isNestedScrollingEnabled = false
        return binding.root
    }
}