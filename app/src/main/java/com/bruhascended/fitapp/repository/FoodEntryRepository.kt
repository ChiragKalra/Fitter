package com.bruhascended.fitapp.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.bruhascended.db.food.FoodEntryDatabaseFactory
import com.bruhascended.db.food.entities.Entry
import com.bruhascended.db.food.entities.Food
import com.bruhascended.db.food.entities.FoodEntry
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty

class FoodEntryRepository(
    mApp: Application
) {

    companion object {
        private var repository: FoodEntryRepository? = null

        class Delegate(
            private val app: Application
        ) {
            operator fun getValue(thisRef: Any?, property: KProperty<*>): FoodEntryRepository {
                if (repository == null) {
                    repository = FoodEntryRepository(app)
                }
                return repository!!
            }
        }
    }


    private val db = FoodEntryDatabaseFactory(mApp).build()

    fun searchConsumedFood(query: String): LiveData<List<Food>> {
        return db.foodManager().searchLive(query)
    }

    suspend fun writeEntry(foodEntry: FoodEntry) = db.insertEntry(foodEntry)

    suspend fun writeEntry(food: Food, entry: Entry) = db.insertEntry(food, entry)

    fun deleteEntry(foodEntry: FoodEntry) = db.deleteEntry(foodEntry)

    fun loadConsumedFoodEntries(): Flow<PagingData<FoodEntry>> {
        return Pager(
            PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,
                prefetchDistance = 60,
                maxSize = 180,
            )
        ) {
            db.loadFoodEntry().allPaged()
        }.flow
    }

    fun loadCount(n: Int): LiveData<List<Food>> = db.foodManager().topNLive(n)

    fun loadLiveFoodEntries(): LiveData<List<FoodEntry>> = db.loadFoodEntry().allLive()
}