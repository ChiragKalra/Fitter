package com.bruhascended.fitapp.util

import android.content.Context
import android.util.Log
import androidx.work.*
import androidx.work.WorkInfo.*
import com.bruhascended.fitapp.workers.ActivityEntryWorker
import com.bruhascended.fitapp.workers.PeriodicEntryWorker
import java.util.*
import java.util.concurrent.TimeUnit

const val SYNC_INTERVAL = 2*60L // seconds
const val IMMEDIATE_TAG = "IMMEDIATE_SYNC"
const val REPEATED_TAG = "REPEATED_SYNC"

fun enqueueSyncJob(context: Context, NAME: String, delay: Long = 0) {
    Log.d("baby", "$NAME enqueued")
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()

    val workRequest: OneTimeWorkRequest.Builder = if (NAME == PeriodicEntryWorker.WORK_NAME)
        OneTimeWorkRequestBuilder<PeriodicEntryWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.SECONDS)
    else
        OneTimeWorkRequestBuilder<ActivityEntryWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delay, TimeUnit.SECONDS)

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

fun enqueueImmediateJob(context: Context, NAME: String) {
    enqueueSyncJob(context, NAME)
}

fun isWorkRequired(time: Long, WORK_NAME: String): Boolean {
    val cal = Calendar.getInstance(TimeZone.getDefault())
    cal.set(2014, 10, 28)
    return time >= cal.timeInMillis
}

fun cancelWork(context: Context, workName: String) {
    Log.d("work_eyo", "Cancelled ,$workName")
    WorkManager.getInstance(context).cancelUniqueWork(workName)
}

fun isWorkScheduled(context: Context, WORK_NAME: String): Boolean {
    val workInfos =
        WorkManager.getInstance(context).getWorkInfosForUniqueWork(WORK_NAME)
            .get()
    var workScheduled = false
    if (workInfos.size == 0 || workInfos == null) return workScheduled
    for (work in workInfos) {
        if (work.state == State.RUNNING || work.state == State.ENQUEUED) {
            workScheduled = true
        }
    }
    return workScheduled
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