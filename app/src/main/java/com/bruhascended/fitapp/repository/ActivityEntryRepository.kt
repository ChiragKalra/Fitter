package com.bruhascended.fitapp.repository

import android.app.Application
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.activity.ActivityEntryDatabase
import com.bruhascended.db.activity.ActivityEntryDatabaseFactory
import com.bruhascended.db.activity.entities.ActivityEntry
import kotlinx.coroutines.flow.Flow
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
}