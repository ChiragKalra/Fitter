package com.bruhascended.fitapp.repository

import android.app.Application
import com.bruhascended.db.friends.entities.DailyStats
import com.bruhascended.db.friends.entities.Friend
import com.bruhascended.db.friends.entities.MonthlyStats
import com.bruhascended.db.friends.entities.WeeklyStats
import com.bruhascended.fitapp.firebase.FriendsFirebaseDao
import com.bruhascended.fitapp.ui.friends.types.FriendStatistics
import com.bruhascended.fitapp.ui.friends.types.Statistic
import com.bruhascended.fitapp.ui.friends.types.TimePeriod
import kotlin.reflect.KProperty

class FriendsRepository {

    companion object {
        private var repository: FriendsRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): FriendsRepository {
            if (repository == null) {
                repository = FriendsRepository()
            }
            return repository!!
        }
    }

    private val firebaseFriendsDb = FriendsFirebaseDao()

    fun flowFriendsStatistics(
        userUid: String,
        timePeriod: TimePeriod,
        sortBy: Statistic = Statistic.CaloriesBurnt,
        callback: (list: List<FriendStatistics>) -> Unit
    ) {
        firebaseFriendsDb.flowFriendsDailyActivity(userUid) { rawStats ->
            val filteredAndSortedStats = rawStats.map { friend ->
                when(timePeriod) {
                    TimePeriod.Daily ->
                        FriendStatistics(friend.username, friend.dailyActivity)
                    TimePeriod.Weekly ->
                        FriendStatistics(friend.username, friend.weeklyActivity)
                    TimePeriod.Monthly ->
                        FriendStatistics(friend.username, friend.monthlyActivity)
                }
            }.sortedByDescending {
                when(sortBy) {
                    Statistic.CaloriesBurnt ->
                        it.totalCalories.toFloat()
                    Statistic.DistanceCovered ->
                        it.totalDistance
                    Statistic.StepsTaken ->
                        it.totalSteps.toFloat()
                    Statistic.TimeActive ->
                        it.totalDuration.toFloat()
                }
            }
            callback(filteredAndSortedStats)
        }
    }

    fun updateUserStatistics(
        userUid: String,
        dailyStats: DailyStats,
        weeklyStats: WeeklyStats,
        monthlyStats: MonthlyStats,
    ) = firebaseFriendsDb.updateUserStatistics(userUid, dailyStats, weeklyStats, monthlyStats)

}
