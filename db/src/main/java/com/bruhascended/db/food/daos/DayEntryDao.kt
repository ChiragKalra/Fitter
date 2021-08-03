package com.bruhascended.db.food.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
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
    """)
    fun getTimeRangeLive (startDay: Long, endDay: Long): LiveData<List<DayEntry>>

    @Query("SELECT * FROM dayEntry ORDER BY day DESC")
    fun loadAllPaged(): PagingSource<Int,DayEntry>

    @Query("SELECT * FROM dayEntry")
    fun loadAllSync(): List<DayEntry>
}
