package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.util.*
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

class PeriodicEntryWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "PERIODIC_WORK"
    }

    override suspend fun doWork(): Result {
        val repo = PreferencesRepository(context)
        if (getAndroidRunTimePermissionGivenMap(
                context,
                permissions.values().toList()
            ).containsValue(false)
        ) {
            cancelWork(context, WORK_NAME)
        } else if (!isOauthPermissionsApproved(context, FitBuilder.fitnessOptions)) {
            cancelWork(context, WORK_NAME)
        }

        try {
            performPeriodicSync(context, repo)
        } catch (e: Exception) {
            Log.d("periodic_eyo", "${e.message}")
            return Result.retry()
        }
        if (repo.getPreference(PreferencesRepository.PreferencesKeys.SYNC_ENABLED).toString()
                .toBooleanStrictOrNull() == true
        )
            enqueueRepeatedJob(context, WORK_NAME)
        return Result.success()
    }
}

fun performPeriodicSync(
    context: Context,
    repo: PreferencesRepository
) {
    val activityEntryRepository: ActivityEntryRepository by ActivityEntryRepository.Delegate(context)

    val lastSyncStartTime =
        repo.getPreference(PreferencesRepository.PreferencesKeys.LAST_PERIODIC_SYNC_TIME) as Long?

    val isWorkImmediate = isWorkImmediate(context, PeriodicEntryWorker.WORK_NAME)
    if (lastSyncStartTime != null && !isWorkImmediate) {
        if (!isWorkRequired(lastSyncStartTime, PeriodicEntryWorker.WORK_NAME))
            cancelWork(context, PeriodicEntryWorker.WORK_NAME)
    }

    var endTime: Long? = null
    var startTime: Long? = null
    val cal = Calendar.getInstance(TimeZone.getDefault())

    if (isWorkImmediate) {
        endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, -6)
        startTime = cal.getTodayStartTime(cal)
    } else {
        if (lastSyncStartTime == null) {
            cal.add(Calendar.DAY_OF_WEEK, -7)
            endTime = cal.getTodayMidnightTime(cal)
            cal.add(Calendar.DAY_OF_WEEK, -6)
            startTime = cal.getTodayStartTime(cal)
        } else {
            cal.timeInMillis = lastSyncStartTime
            cal.add(Calendar.DAY_OF_WEEK, -1)
            endTime = cal.getTodayMidnightTime(cal)
            cal.add(Calendar.DAY_OF_WEEK, -6)
            startTime = cal.getTodayStartTime(cal)
        }
    }

    val estimatedStepSource = FitBuilder.estimatedStepSource

    val readRequest = DataReadRequest.Builder()
        .bucketByTime(30, TimeUnit.MINUTES)
        .aggregate(DataType.TYPE_CALORIES_EXPENDED)
        .aggregate(estimatedStepSource)
        .aggregate(DataType.TYPE_DISTANCE_DELTA)
        .aggregate(DataType.TYPE_MOVE_MINUTES)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .enableServerQueries()
        .build()

    val client =
        Fitness.getHistoryClient(context, getGoogleAccount(context, FitBuilder.fitnessOptions))
    val task = client.readData(readRequest)
    val result = Tasks.await(task.addOnFailureListener {
        Log.d("periodic_eyo", "${it.message}")
    })

    try {
        if (result.status.isSuccess) {
            runBlocking {
                val entries = dumpDayEntryBuckets(result.buckets)
                insertPeriodicEntriesToDb(entries, activityEntryRepository)
                if (!isWorkImmediate) {
                    repo.updatePreference(
                        PreferencesRepository.PreferencesKeys.LAST_PERIODIC_SYNC_TIME,
                        startTime
                    )
                }
            }
            Log.d("periodic_eyo", "${result.buckets.size}")
        }
    } catch (e: Exception) {
        Log.d("periodic_eyo", "${e.message}")
    }
}



