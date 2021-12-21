package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.lifecycle.asFlow
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.db.friends.entities.DailyStats
import com.bruhascended.db.friends.entities.MonthlyStats
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
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = cal.time
        val startDate = cal.apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time
        var firebaseAuthUid = FirebaseAuth.getInstance().uid!!
        val friendsRepo by FriendsRepository.Delegate()
        val activityRepo by ActivityEntryRepository.Delegate(context)
        if (getCurrentAccount(context) != null) {
            val data = activityRepo.loadRangeDayEntries(startDate, endDate).asFlow().first()
            val monthlyStats = mutableListOf<MonthlyStats>()
            data.forEach {
                //val item = MonthlyStats(it.totalCalories.toInt(),it.)
                //monthlyStats.add()
            }

        }
        return Result.success()
    }
}