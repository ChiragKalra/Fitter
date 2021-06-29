package com.bruhascended.fitapp.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bruhascended.fitapp.databinding.FragmentJournalActivityBinding

class ActivityJournalFragment: Fragment() {

    private lateinit var binding: FragmentJournalActivityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentJournalActivityBinding.inflate(inflater, container, false)

        return binding.root
    }
}