package com.bruhascended.fitapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.repository.PreferencesRepository.PreferencesKeys
import com.bruhascended.fitapp.util.*
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Tasks
import java.util.*
import java.util.concurrent.TimeUnit

class DayEntryWorker(val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "DAILY_WORK"
    }

    val repo = PreferencesRepository(context)
    override suspend fun doWork(): Result {

        if (getAndroidRunTimePermissionGivenMap(
                context,
                permissions.values().toList()
            ).containsValue(false)
        ) {
            // todo
            return Result.failure()
        } else if (!isOauthPermissionsApproved(context, FitBuilder.fitnessOptions)) {
            // todo
            return Result.failure()
        }

        try {
            performAction()
        } catch (e: Exception) {
            // todo notify user
            Log.d("daily_eyo", "${e.message}")
            return Result.retry()
        }

        if (repo.getPreference(PreferencesKeys.SYNC_ENABLED).toString()
                .toBooleanStrictOrNull() == true
        )
            enqueueRepeatedJob(context, WORK_NAME)

        return Result.success()

    }

    private fun performAction() {
        var endTime: Long? = null
        var startTime: Long? = null
        val cal = Calendar.getInstance(TimeZone.getDefault())

        val isWorkImmediate = isWorkImmediate(context, PeriodicEntryWorker.WORK_NAME)

        val lastSyncStartTime: Long? =
            repo.getPreference(PreferencesKeys.LAST_DAILY_SYNC_TIME) as Long?

        if (lastSyncStartTime != null && !isWorkImmediate) {
            if (!isWorkRequired(lastSyncStartTime, WORK_NAME))
                cancelWork(context, WORK_NAME)
        }

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

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .read(DataType.TYPE_MOVE_MINUTES)
            .enableServerQueries()
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        val client =
            Fitness.getHistoryClient(context, getGoogleAccount(context, FitBuilder.fitnessOptions))
        val task = client.readData(readRequest)
        val result = Tasks.await(task.addOnFailureListener {
            Log.d("periodic_eyo", "${it.message}")
        })

        try {
            if (result.status.isSuccess) {
                // todo store to db
                if (!isWorkImmediate) {
                    repo.updatePreference(PreferencesKeys.LAST_DAILY_SYNC_TIME, startTime)
                }
                Log.d("daily_eyo", "${result.buckets}")
            } else {
                // todo notify user
                Log.d("daily_eyo", "${result.status}")
            }
        } catch (e: Exception) {
            // todo notify user
            Log.d("daily_eyo", "${e.message}")
        }
    }
}