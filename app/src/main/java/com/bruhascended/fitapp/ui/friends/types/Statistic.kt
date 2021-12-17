package com.bruhascended.fitapp.ui.friends.types

import androidx.annotation.StringRes
import com.bruhascended.fitapp.R

enum class Statistic(
	@StringRes
	val stringRes: Int
) {
	DistanceCovered(R.string.distance),
	StepsTaken(R.string.steps),
	TimeActive(R.string.time_active),
}