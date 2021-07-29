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
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataSource
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

class PeriodicEntryWorker(
    val context: Context,
    params: WorkerParameters
) :
    CoroutineWorker(context, params) {

    companion object {
        val name = "PeriodicEntry"
        val fitnessOptions: FitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .build()
    }

    override suspend fun doWork(): Result {
        if (getAndroidRunTimePermissionGivenMap(
                context,
                permissions.values().toList()
            ).containsValue(false)
        ) {
            return Result.failure()
        } else if (!isOauthPermissionsApproved(context, fitnessOptions)) {
            return Result.failure()
        }

        val UserRepository = UserPreferenceRepository(context)
        val activityEntryRepository by ActivityEntryRepository.Delegate(Application())

        try {
            UserRepository.userPreferencesFLow.collect { preference ->
                if (preference.syncEnabled) {
                    performSync(context, preference.lastSyncStartTime, UserRepository)
                }
            }
            Log.d("eyo", "Sync is run")
        } catch (e: Exception) {
            Log.d("eyo", "${e.message}")
            return Result.retry()
        }
        return Result.success()
    }
}

private fun performSync(
    context: Context,
    lastSyncStartTime: Long?,
    repository: UserPreferenceRepository
) {
    var endTime: Long? = null
    var startTime: Long? = null
    val cal = Calendar.getInstance(TimeZone.getDefault())

    if (lastSyncStartTime == null) {
        cal.add(Calendar.DAY_OF_WEEK, -6)
        endTime = cal.getTodayMidnightTime(cal)
        cal.add(Calendar.WEEK_OF_MONTH, -1)
        startTime = cal.getTodayStartTime(cal)
    } else {
        cal.timeInMillis = lastSyncStartTime
        cal.add(Calendar.DAY_OF_WEEK, -1)
        endTime = cal.getTodayMidnightTime(cal)
        cal.add(Calendar.DAY_OF_WEEK, -6)
        startTime = cal.getTodayStartTime(cal)
    }

    Log.d("eyo", "${DateTimePresenter(context, startTime).fullTimeAndDate}")
    Log.d("eyo", "${DateTimePresenter(context, endTime).fullTimeAndDate}")

    val estimatedStepSource = DataSource.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setType(DataSource.TYPE_DERIVED)
        .setStreamName("estimated_steps")
        .setAppPackageName("com.google.android.gms")
        .build()

    val readRequest = DataReadRequest.Builder()
        .bucketByTime(30, TimeUnit.MINUTES)
        .aggregate(DataType.TYPE_CALORIES_EXPENDED)
        .aggregate(estimatedStepSource)
        .aggregate(DataType.TYPE_DISTANCE_DELTA)
        .aggregate(DataType.TYPE_MOVE_MINUTES)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .enableServerQueries()
        .build()

    Fitness.getHistoryClient(context, getGoogleAccount(context, PeriodicEntryWorker.fitnessOptions))
        .readData(readRequest)
        .addOnSuccessListener {
            CoroutineScope(IO).launch {
                try {
                    repository.updateLastSyncTime(startTime)
                } catch (e: Exception) {
                    Log.d("eyo", "${e.message}")
                }
            }
            Log.d("eyo", "${it.buckets.size}")
        }
        .addOnFailureListener {
            Log.d("eyo", "${it.message}")
        }
}