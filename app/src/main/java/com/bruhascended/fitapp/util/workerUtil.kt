package com.bruhascended.fitapp.util

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.WorkInfo.*
import com.bruhascended.fitapp.workers.ActivityEntryWorker
import com.bruhascended.fitapp.workers.DayEntryWorker
import com.bruhascended.fitapp.workers.PeriodicEntryWorker
import java.util.*
import java.util.concurrent.TimeUnit

const val SYNC_INTERVAL = 10L // seconds
const val IMMEDIATE_TAG = "IMMEDIATE_SYNC"
const val REPEATED_TAG = "REPEATED_SYNC"

fun enqueueSyncJob(context: Context, NAME: String, delay: Long = 0) {
    Log.d("baby", "$NAME enqueued")
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val workRequest: OneTimeWorkRequest.Builder = when (NAME) {
        PeriodicEntryWorker.WORK_NAME -> {
            OneTimeWorkRequestBuilder<PeriodicEntryWorker>()
                .setConstraints(constraints)
                .setInitialDelay(delay, TimeUnit.SECONDS)
        }
        ActivityEntryWorker.WORK_NAME -> {
            OneTimeWorkRequestBuilder<ActivityEntryWorker>()
                .setConstraints(constraints)
                .setInitialDelay(delay, TimeUnit.SECONDS)
        }
        else -> {
            OneTimeWorkRequestBuilder<DayEntryWorker>()
                .setConstraints(constraints)
                .setInitialDelay(delay, TimeUnit.SECONDS)
        }
    }

    if (delay == 0L) {
        workRequest.addTag(IMMEDIATE_TAG)
    } else {
        workRequest.addTag(REPEATED_TAG)
    }

    WorkManager.getInstance(context)
        .enqueueUniqueWork(
            NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest.build()
        )
}

fun enqueueRepeatedJob(context: Context, NAME: String) {
    enqueueSyncJob(context, NAME, SYNC_INTERVAL)
}

fun enqueueImmediateJob(context: Context) {
    enqueueSyncJob(context, DayEntryWorker.WORK_NAME)
    enqueueSyncJob(context, ActivityEntryWorker.WORK_NAME)
    enqueueSyncJob(context, PeriodicEntryWorker.WORK_NAME)
}

fun isWorkRequired(time: Long, WORK_NAME: String): Boolean {
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.set(2014, 10, 28)
    return time >= cal.timeInMillis
}

fun cancelWork(context: Context, name: String? = null) {
    if (name == null) {
        WorkManager.getInstance(context).cancelAllWorkByTag(IMMEDIATE_TAG)
        WorkManager.getInstance(context).cancelAllWorkByTag(REPEATED_TAG)
    } else {
        WorkManager.getInstance(context).cancelUniqueWork(name)
    }
}

fun isWorkImmediate(context: Context, WORK_NAME: String): Boolean {
    val workInfos =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME)
            .get()
    for (work in workInfos) {
        if (work.tags.contains(IMMEDIATE_TAG)) {
            return true
        }
    }
    return false
}

fun isWorkScheduled(context: Context): Boolean {
    val workManger = WorkManager.getInstance(context)
    val workInfos = workManger.getWorkInfosByTag(REPEATED_TAG).get()
    workInfos.addAll(workManger.getWorkInfosByTag(IMMEDIATE_TAG).get())
    var workScheduled = false
    if (workInfos.size == 0 || workInfos == null) return workScheduled
    for (work in workInfos) {
        if (work.state == State.RUNNING || work.state == State.ENQUEUED) {
            workScheduled = true
        }
    }
    return workScheduled
}