package com.bruhascended.fitapp.ui.friends.types

import androidx.annotation.StringRes
import com.bruhascended.fitapp.R

enum class Statistic(
	@StringRes
	val stringRes: Int
) {
	TimeActive(R.string.time_active),
	StepsTaken(R.string.steps),
	DistanceCovered(R.string.distance),
}