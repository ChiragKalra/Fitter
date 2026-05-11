package com.bruhascended.fitapp.ui.dashboard

import androidx.annotation.DrawableRes
import com.bruhascended.fitapp.R

@DrawableRes
fun DashboardSection.listPreviewIcon(): Int =
    when (this) {
        DashboardSection.SUMMARY_RING -> R.drawable.ic_dashboard
        DashboardSection.TODAY_STATS -> R.drawable.ic_journal
        DashboardSection.STEPS_WEEK -> R.drawable.ic_steps
        DashboardSection.ENERGY_WEEK -> R.drawable.ic_energy_burn
        DashboardSection.NUTRIENTS -> R.drawable.ic_consumed
    }
