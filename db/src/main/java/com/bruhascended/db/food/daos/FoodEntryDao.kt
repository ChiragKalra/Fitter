package com.bruhascended.db.food.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.food.entities.FoodEntry

@Dao
interface FoodEntryDao {
    @Transaction
    @Query("SELECT * FROM entry WHERE entryId LIKE :id")
    fun singleById (id: Long): FoodEntry?

    @Transaction
    @Query("SELECT * FROM entry WHERE entryId LIKE :id")
    fun singleLiveById (id: Long): LiveData<FoodEntry?>

    @Transaction
    @Query("SELECT * FROM entry ORDER BY timeInMillis DESC, mealType ASC")
    fun allPaged(): PagingSource<Int, FoodEntry>

    @Transaction
    @Query("SELECT * FROM entry ORDER BY timeInMillis DESC, mealType ASC")
    fun allSync(): List<FoodEntry>

    @Transaction
    @Query("SELECT * FROM entry ORDER BY timeInMillis DESC, mealType ASC")
    fun allLive(): LiveData<List<FoodEntry>>

    @Query("SELECT COUNT(entryId) FROM entry")
    fun liveCount(): LiveData<Int>
}