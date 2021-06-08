package com.bruhascended.fitapp.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bruhascended.fitapp.databinding.FragmentJournalFoodBinding

class FoodJournalFragment: Fragment() {

    private lateinit var binding: FragmentJournalFoodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentJournalFoodBinding.inflate(inflater, container, false)

        return binding.root
    }
}