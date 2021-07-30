package com.bruhascended.fitapp.workers

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.UserPreferenceRepository
import com.bruhascended.fitapp.ui.main.permissions
import com.bruhascended.fitapp.util.*
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityEntryWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (getAndroidRunTimePermissionGivenMap(
                context,
                permissions.values().toList()
            ).containsValue(false)
        ) {
            return Result.failure()
        } else if (!isOauthPermissionsApproved(context, FitBuilder.fitnessOptions)) {
            return Result.failure()
        }

        val userRepository = UserPreferenceRepository(context)
        val activityEntryRepository by ActivityEntryRepository.Delegate(Application())

        try {
            userRepository.userPreferencesFLow.collect { preference ->
                if (preference.syncEnabled) {
                    performActivitySync(
                        context,
                        preference.lastActivitySyncStartTime,
                        userRepository,
                        activityEntryRepository
                    )
                }
            }
        } catch (e: Exception) {
            Log.d("activity_eyo", "${e.message}")
            return Result.retry()
        }
        return Result.success()
    }

    private fun performActivitySync(
        context: Context,
        lastActivitySyncStartTime: Long?,
        userRepository: UserPreferenceRepository,
        activityEntryRepository: ActivityEntryRepository
    ) {
        var endTime: Long? = null
        var startTime: Long? = null
        val cal = Calendar.getInstance(TimeZone.getDefault())

        if (lastActivitySyncStartTime != null) {
            cal.timeInMillis = lastActivitySyncStartTime
        }
        cal.add(Calendar.DAY_OF_WEEK, -1)
        endTime = cal.getTodayMidnightTime(cal)
        startTime = cal.getTodayStartTime(cal)

        Log.d("activity_eyo", "${DateTimePresenter(context, startTime).fullTimeAndDate}")
        Log.d("activity_eyo", "${DateTimePresenter(context, endTime).fullTimeAndDate}")

        val estimatedStepSource = FitBuilder.estimatedStepSource

        val readRequest = DataReadRequest.Builder()
            .bucketByActivitySegment(5, TimeUnit.MINUTES)
            .aggregate(estimatedStepSource)
            .aggregate(DataType.TYPE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context, FitBuilder.fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener {
                CoroutineScope(IO).launch {
                    try {
                        userRepository.updateLastActivitySyncTime(startTime)
                        for (entry in dumpActivityEntryBuckets(it.buckets)) {
                            activityEntryRepository.writeEntry(entry)
                        }
                    } catch (e: Exception) {
                        Log.d("activity_eyo", "${e.message}")
                    }
                }
            }
            .addOnFailureListener {
                Log.d("activity_eyo", "${it.message}")
            }

    }
}