package com.bruhascended.db.weight.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.bruhascended.db.weight.entities.WeightEntry

@Dao
interface WeightEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (weightEntry: WeightEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(weightEntries: List<WeightEntry>): List<Long>

    @Delete
    fun delete (weightEntry: WeightEntry)

    @Query("DELETE FROM weight_entry")
    fun deleteAll()

    @Query("SELECT * FROM weight_entry WHERE id LIKE :id")
    fun findById (id: Long): WeightEntry?

    @Query("SELECT * FROM weight_entry WHERE id LIKE :id")
    fun loadLive (id: Long): LiveData<WeightEntry?>

    @Query("SELECT * FROM weight_entry ORDER BY timeInMillis DESC")
    fun loadAllPaged(): PagingSource<Int, WeightEntry>

    @Query("SELECT * FROM weight_entry ORDER BY timeInMillis DESC")
    fun loadAllSync(): List<WeightEntry>

    @Query("SELECT * FROM weight_entry ORDER BY timeInMillis DESC")
    fun loadAllLive(): LiveData<List<WeightEntry>>

    @Query("""
        SELECT *
        FROM weight_entry
        WHERE timeInMillis >= :startTime AND timeInMillis < :endTime
        ORDER BY timeInMillis ASC
    """)
    fun getTimeRangeSync(startTime: Long, endTime: Long): List<WeightEntry>

    @Query("""
        SELECT *
        FROM weight_entry
        WHERE timeInMillis >= :startTime AND timeInMillis < :endTime
        ORDER BY timeInMillis ASC
    """)
    fun getTimeRangeLive(startTime: Long, endTime: Long): LiveData<List<WeightEntry>>

    @Query("""
        SELECT *
        FROM weight_entry
        WHERE timeInMillis >= :startTime AND timeInMillis < :endTime
        ORDER BY timeInMillis ASC
    """)
    fun getTimeRangeFlow(startTime: Long, endTime: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entry ORDER BY timeInMillis DESC LIMIT 1")
    fun latestSync(): WeightEntry?

    @Query("SELECT * FROM weight_entry ORDER BY timeInMillis DESC LIMIT 1")
    fun latestLive(): LiveData<WeightEntry?>

    @Query("SELECT COUNT(id) FROM weight_entry")
    fun getLiveCount(): LiveData<Int>

    @Query("SELECT * FROM weight_entry WHERE hcId LIKE :hcId LIMIT 1")
    fun findByHcId(hcId: String): WeightEntry?

    @Query("DELETE FROM weight_entry WHERE hcId LIKE :hcId")
    fun deleteByHcId(hcId: String)
}
