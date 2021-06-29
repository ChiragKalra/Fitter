package com.bruhascended.fitapp.ui.journal

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bruhascended.fitapp.ui.foodjournal.FoodJournalFragment

class ViewPagerAdaptor (
    fm: FragmentManager,
    lc: Lifecycle
): FragmentStateAdapter(fm, lc) {
    override fun createFragment (position: Int) =
        if (position == 0) FoodJournalFragment() else ActivityJournalFragment()

    override fun getItemCount() = 2
}