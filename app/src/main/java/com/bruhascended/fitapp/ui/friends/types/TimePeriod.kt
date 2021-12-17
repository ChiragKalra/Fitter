package com.bruhascended.fitapp.ui.friends.types

import androidx.annotation.StringRes
import com.bruhascended.fitapp.R

enum class TimePeriod(
	@StringRes
	val stringRes: Int
) {
	Daily(R.string.today),
	Weekly(R.string.this_week),
	Monthly(R.string.monthly)
}