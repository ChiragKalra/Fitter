package com.bruhascended.fitapp.ui.friends.types

import com.bruhascended.db.friends.entities.DailyStats
import com.bruhascended.db.friends.entities.MonthlyStats
import com.bruhascended.db.friends.entities.WeeklyStats

data class FriendStatistics(
	val username: String,
	var totalSteps: Int = 0,
	var totalCalories: Int = 0,
	var totalDuration: Int = 0,
	var totalDistance: Float = 0f,
) {
	constructor(username: String, stats: DailyStats):
		this(
			username,
			totalSteps = stats.totalSteps,
			totalDuration = stats.totalDuration,
			totalDistance = stats.totalDistance,
			totalCalories = stats.totalCalories,
		)

	constructor(username: String, stats: WeeklyStats):
		this(
			username,
			totalSteps = stats.totalSteps,
			totalDuration = stats.totalDuration,
			totalDistance = stats.totalDistance,
			totalCalories = stats.totalCalories,
		)

	constructor(username: String, stats: MonthlyStats):
		this(
			username,
			totalSteps = stats.totalSteps,
			totalDuration = stats.totalDuration,
			totalDistance = stats.totalDistance,
			totalCalories = stats.totalCalories,
		)
}
