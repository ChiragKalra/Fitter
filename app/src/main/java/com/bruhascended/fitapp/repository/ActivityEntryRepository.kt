package com.bruhascended.fitapp.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.activity.ActivityEntryDatabase
import com.bruhascended.db.activity.ActivityEntryDatabaseFactory
import com.bruhascended.db.activity.entities.ActivityEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.reflect.KProperty

class ActivityEntryRepository(
    mApp: Application
) {

    companion object {
        private var repository: ActivityEntryRepository? = null
    }

    class Delegate(
        private val app: Application
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ActivityEntryRepository {
            if (repository == null) {
                repository = ActivityEntryRepository(app)
            }
            return repository!!
        }
    }


    data class SeparatorInfo (
        var totalCalories: Int = 0,
        var totalDuration: Long = 0L,
        var totalDistance: Double = .0,
        var totalSteps: Int = 0,
    ) {

        operator fun plusAssign(entry: ActivityEntry) {
            totalCalories += entry.calories
            totalSteps += entry.steps ?: 0
            totalDuration += entry.duration ?: 0
            totalDistance += entry.distance ?: .0
        }

        operator fun plus(entry: ActivityEntry) = SeparatorInfo(
            totalCalories + entry.calories,
            totalDuration + (entry.duration ?: 0),
            totalDistance + (entry.distance ?: .0),
            totalSteps + (entry.steps ?: 0),
        )
    }


    private val db: ActivityEntryDatabase  = ActivityEntryDatabaseFactory(mApp).build()

    suspend fun writeEntry(entry: ActivityEntry) = db.entryManager().insert(entry)

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

    fun loadLiveActivityEntries() = db.entryManager().loadAllLive()

    fun loadLiveSeparators(): MutableLiveData<HashMap<Date, SeparatorInfo>> {
        val liveInfoMap = MutableLiveData<HashMap<Date, SeparatorInfo>>()
        loadLiveActivityEntries().observeForever { all ->
            CoroutineScope(Dispatchers.IO).launch {
                val infoMap = HashMap<Date, SeparatorInfo>()
                all.forEach {
                    val date = it.date
                    if (!infoMap.containsKey(date)) {
                        infoMap[date] = SeparatorInfo()
                    }
                    infoMap[date]?.also { info -> info += it }
                }
                liveInfoMap.postValue(infoMap)
            }
        }
        return liveInfoMap
    }

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
                        allArr.slice( 0 until all.size - 1).forEachIndexed { ind, entry ->
                            if (entry.date != allArr[ind+1].date) {
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
}