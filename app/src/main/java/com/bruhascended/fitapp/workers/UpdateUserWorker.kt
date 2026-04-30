package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.db.friends.entities.DailyStats
import com.bruhascended.db.friends.entities.MonthlyStats
import com.bruhascended.db.friends.entities.WeeklyStats
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FriendsRepository
import com.bruhascended.fitapp.util.enqueueRepeatedJob
import com.bruhascended.fitapp.util.getCurrentAccount
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import java.text.DateFormat
import java.util.*

class UpdateUserWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "PERIODIC_USER_UPDATE_WORK"
    }

    override suspend fun doWork(): Result {
        var firebaseAuthUid = FirebaseAuth.getInstance().uid!!
        val friendsRepo by FriendsRepository.Delegate()
        val activityRepo by ActivityEntryRepository.Delegate(context)
        if (getCurrentAccount(context) != null) {
            val monthlyData = activityRepo.loadLastMonthDayEntries().asFlow().first()
            val weeklyData = activityRepo.loadLastWeekDayEntries().asFlow().first()

            val dailyStats = DailyStats()
            val weeklyStats = WeeklyStats()
            val monthlyStats = MonthlyStats()

            monthlyData.forEach {
                val item = MonthlyStats(
                    it.totalCalories.toInt(),
                    (it.totalDuration / 60000f).toInt(),
                    it.totalDistance.toFloat()*1000f,
                    it.totalSteps
                )
                monthlyStats += item
            }
            weeklyData.forEach {
                val item = WeeklyStats(
                    it.totalCalories.toInt(),
                    (it.totalDuration / 60000f).toInt(),
                    it.totalDistance.toFloat()*1000f,
                    it.totalSteps
                )
                weeklyStats += item
            }
            val today = weeklyData.last()
            val item = DailyStats(
                today.totalCalories.toInt(),
                (today.totalDuration / 60000f).toInt(),
                today.totalDistance.toFloat()*1000f,
                today.totalSteps
            )
            dailyStats += item
            friendsRepo.updateUserStatistics(firebaseAuthUid,dailyStats,weeklyStats,monthlyStats)
        }
        return Result.success()
    }
}