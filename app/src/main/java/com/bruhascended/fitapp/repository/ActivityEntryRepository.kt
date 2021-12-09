package com.bruhascended.fitapp.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.activity.ActivityEntryDatabase
import com.bruhascended.db.activity.ActivityEntryDatabaseFactory
import com.bruhascended.db.activity.entities.ActivityEntry
import com.bruhascended.db.activity.entities.PeriodicEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashSet
import kotlin.reflect.KProperty

class ActivityEntryRepository(
    context: Context
) {

    companion object {
        private var repository: ActivityEntryRepository? = null
    }

    class Delegate(
        private val context: Context
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityEntryRepository {
            if (repository == null) {
                repository = ActivityEntryRepository(context)
            }
            return repository!!
        }
    }

    private val db: ActivityEntryDatabase = ActivityEntryDatabaseFactory(context).build()

    // periodic entry fun's
    fun insertPeriodicEntries(periodicEntries: List<PeriodicEntry>) =
        db.periodicEntryManager().insertAll(periodicEntries)

    fun insertPeriodicEntry(periodicEntry: PeriodicEntry) =
        db.periodicEntryManager().insert(periodicEntry)

    fun findPeriodicEntryByStartTime(timeInMillis: Long) =
        db.periodicEntryManager().findByStartTime(timeInMillis)

    // activity entry fun's
    fun writeEntry(entry: ActivityEntry) = db.entryManager().insert(entry)

    fun findByStartTime(timeInMillis: Long) = db.entryManager().findByStartTime(timeInMillis)

    fun deleteEntry(entry: ActivityEntry) = db.entryManager().delete(entry)

    fun loadActivityEntries(): Flow<PagingData<ActivityEntry>> {
        return Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 60,
                maxSize = 180,
            )
        ) {
            db.entryManager().loadAllPaged()
        }.flow
    }

    private fun loadLiveActivityEntries() = db.entryManager().loadAllLive()

    fun loadLiveSeparatorAt(date: Date) = db.getLivePeriodicEntryOf(date)

    fun loadLiveLastItems(): MutableLiveData<HashSet<Long>> {
        val liveLastIds = MutableLiveData<HashSet<Long>>()
        loadLiveActivityEntries().observeForever { all ->
            CoroutineScope(Dispatchers.IO).launch {
                val allArr = all.toTypedArray()
                val newIdSet = HashSet<Long>().apply {
                    if (allArr.isNotEmpty()) {
                        add(allArr.last().id!!)
                    }
                    if (allArr.size > 1) {
                        allArr.slice(0 until all.size - 1).forEachIndexed { ind, entry ->
                            if (entry.date != allArr[ind + 1].date) {
                                add(entry.id!!)
                            }
                        }
                    }
                }
                liveLastIds.postValue(newIdSet)
            }
        }
        return liveLastIds
    }

    fun loadLastWeekPeriodicEntries(): LiveData<List<PeriodicEntry>> {
        val date = Calendar.getInstance().apply {
            set(Calendar.MILLISECOND, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.HOUR, 0)
        }.time
        return db.getLivePeriodicEntryWeekly(date)
    }

    fun loadRangePeriodicEntries(startDate: Date, endDate: Date) =
        db.getLivePeriodicEntryOver(startDate, endDate)

    fun loadRangeTotalEntry(startDate: Date, endDate: Date) =
        db.getLiveTotalEntryOver(startDate, endDate)

}