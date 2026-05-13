package com.bruhascended.db.food.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.bruhascended.db.food.entities.DayEntry

@Dao
interface DayEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (dayEntry: DayEntry): Long

    @Delete
    fun delete (dayEntry: DayEntry)

    @Query("SELECT * FROM dayEntry WHERE day LIKE :day")
    fun findByDay (day: Long): DayEntry?

    @Query("SELECT * FROM dayEntry WHERE day LIKE :day")
    fun getLive (day: Long): LiveData<DayEntry?>


    @Query("""
        SELECT * 
        FROM dayEntry
        WHERE day >= :startDay AND day < :endDay
        ORDER BY day ASC
    """)
    fun getTimeRangeLive (startDay: Long, endDay: Long): LiveData<List<DayEntry>>

    @Query("""
        SELECT * 
        FROM dayEntry
        WHERE day >= :startDay AND day < :endDay
        ORDER BY day ASC
    """)
    fun getTimeRangeFlow (startDay: Long, endDay: Long): Flow<List<DayEntry>>

    @Query("""
        SELECT *
        FROM dayEntry
        WHERE day >= :startDay AND day < :endDay
        ORDER BY day ASC
    """)
    fun getTimeRangeSync(startDay: Long, endDay: Long): List<DayEntry>

    @Query("SELECT * FROM dayEntry ORDER BY day DESC")
    fun loadAllPaged(): PagingSource<Int,DayEntry>

    @Query("SELECT * FROM dayEntry")
    fun loadAllSync(): List<DayEntry>

    @Query("DELETE FROM dayEntry")
    fun deleteAll()
}
