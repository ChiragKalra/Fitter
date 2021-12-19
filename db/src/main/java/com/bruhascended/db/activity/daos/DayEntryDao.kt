package com.bruhascended.db.activity.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.activity.entities.DayEntry

@Dao
interface DayEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (activity_day_entries: DayEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll (dayEntries: List<DayEntry>): List<Long>

    @Delete
    fun delete (dayEntry: DayEntry)

    @Query("SELECT * FROM activity_day_entries WHERE startTime LIKE :time")
    fun findByStartTime (time: Long): DayEntry?

    @Query("SELECT * FROM activity_day_entries WHERE startTime LIKE :time")
    fun getLiveByStartTime (time: Long): LiveData<DayEntry?>

    @Query("SELECT * FROM activity_day_entries ORDER BY startTime DESC")
    fun loadAllPaged(): PagingSource<Int, DayEntry>

    @Query("SELECT * FROM activity_day_entries")
    fun loadAllSync(): List<DayEntry>

    @Query("""
        SELECT * 
        FROM activity_day_entries 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSync (startTime: Long, endTime: Long): List<DayEntry>

    @Query("""
        SELECT * 
        FROM activity_day_entries 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangePaged (startTime: Long, endTime: Long): PagingSource<Int, DayEntry>

    @Query("""
        SELECT * 
        FROM activity_day_entries 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeLive (startTime: Long, endTime: Long): LiveData<List<DayEntry>>


    @Query("""
        SELECT :startTime as startTime, 
                SUM(totalCalories) as totalCalories, 
                SUM(totalDuration) as totalDuration, 
                SUM(totalDistance) as totalDistance, 
                SUM(totalSteps) as totalSteps 
        FROM activity_day_entries 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSumSync (startTime: Long, endTime: Long): DayEntry

    @Query("""
        SELECT :startTime as startTime, 
                SUM(totalCalories) as totalCalories, 
                SUM(totalDuration) as totalDuration, 
                SUM(totalDistance) as totalDistance, 
                SUM(totalSteps) as totalSteps 
        FROM activity_day_entries 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSumLive (startTime: Long, endTime: Long): LiveData<DayEntry>
} 
