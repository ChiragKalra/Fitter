package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.util.*
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit

class ActivityEntryWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "ACTIVITY_WORK"
    }

    override suspend fun doWork(): Result {
        val repo = PreferencesRepository(context)
        if (getAndroidRunTimePermissionGivenMap(
                context,
                permissions.values().toList()
            ).containsValue(false)
        ) {
            // todo notification to user regarding sync failure
            cancelWork(context, WORK_NAME)
        } else if (!isOauthPermissionsApproved(context, FitBuilder.fitnessOptions)) {
            // todo notification to user regarding sync failure
            cancelWork(context, WORK_NAME)
        }

        try {
            performActivitySync(context, repo)
        } catch (e: Exception) {
            Log.d("activity_eyo", "${e.message}")
            return Result.retry()
        }
        if (repo.getPreference(PreferencesKeys.SYNC_ENABLED).toString()
                .toBooleanStrictOrNull() == true
        )
            enqueueRepeatedJob(context, WORK_NAME)
        return Result.success()
    }
}

fun performActivitySync(context: Context, repo: PreferencesRepository) {
    val activityEntryRepository by ActivityEntryRepository.Delegate(context)

    val lastSyncStartTime =
        repo.getPreference(PreferencesKeys.LAST_ACTIVITY_SYNC_TIME) as Long?


    val isWorkImmediate = isWorkImmediate(context, ActivityEntryWorker.WORK_NAME)

    if (lastSyncStartTime != null && !isWorkImmediate) {
        if (!isWorkRequired(lastSyncStartTime, ActivityEntryWorker.WORK_NAME))
            cancelWork(context, ActivityEntryWorker.WORK_NAME)
    }

    var endTime: Long? = null
    var startTime: Long? = null
    val cal = Calendar.getInstance(TimeZone.getDefault())

    if (isWorkImmediate) {
        endTime = cal.timeInMillis
        startTime = cal.getTodayStartTime(cal)
    } else {
        if (lastSyncStartTime != null) {
            cal.timeInMillis = lastSyncStartTime
        }
        cal.add(Calendar.DAY_OF_WEEK, -1)
        endTime = cal.getTodayMidnightTime(cal)
        startTime = cal.getTodayStartTime(cal)
    }

    val estimatedStepSource = FitBuilder.estimatedStepSource

    for (day in 1..7) {
        val readRequest = DataReadRequest.Builder()
            .bucketByActivitySegment(5, TimeUnit.MINUTES)
            .aggregate(estimatedStepSource)
            .aggregate(DataType.TYPE_DISTANCE_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED)
            .enableServerQueries()
            .setTimeRange(startTime!!, endTime!!, TimeUnit.MILLISECONDS)
            .build()
        val client =
            Fitness.getHistoryClient(context, getGoogleAccount(context, FitBuilder.fitnessOptions))
        val task = client.readData(readRequest)
        val result = Tasks.await(task.addOnFailureListener {
            Log.d("activity_eyo", "${it.message}")
        })

        try {
            if (result.status.isSuccess) {
                runBlocking {
                    val entries = dumpActivityEntryBuckets(result.buckets)
                    insertActivityEntriesToDb(entries, activityEntryRepository)
                    if (!isWorkImmediate) {
                        repo.updatePreference(
                            PreferencesKeys.LAST_ACTIVITY_SYNC_TIME,
                            startTime!!
                        )
                    }
                }
                Log.d("activity_eyo", "${result.buckets.size}")
            }
        } catch (e: Exception) {
            Log.d("activity_eyo", "${e.message}")
            break
        }
        cal.add(Calendar.DAY_OF_WEEK, -1)
        startTime = cal.getTodayStartTime(cal)
        endTime = cal.getTodayMidnightTime(cal)
    }
}