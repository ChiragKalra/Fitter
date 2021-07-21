package com.bruhascended.db.activity.daos

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.*
import com.bruhascended.db.activity.entities.PeriodicEntry

@Dao
interface PeriodicEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (periodicEntry: PeriodicEntry): Long

    @Delete
    fun delete (periodicEntry: PeriodicEntry)

    @Query("SELECT * FROM periodicEntry WHERE startTime LIKE :time")
    fun findByStartTime (time: Long): PeriodicEntry?

    @Query("SELECT * FROM periodicEntry WHERE startTime LIKE :time")
    fun getLiveByStartTime (time: Long): LiveData<PeriodicEntry?>

    @Query("SELECT * FROM periodicEntry ORDER BY startTime DESC")
    fun loadAllPaged(): PagingSource<Int, PeriodicEntry>

    @Query("SELECT * FROM periodicEntry")
    fun loadAllSync(): List<PeriodicEntry>

    @Query("""
        SELECT * 
        FROM periodicEntry 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSync (startTime: Long, endTime: Long): List<PeriodicEntry>

    @Query("""
        SELECT * 
        FROM periodicEntry 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangePaged (startTime: Long, endTime: Long): PagingSource<Int, PeriodicEntry>

    @Query("""
        SELECT * 
        FROM periodicEntry 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeLive (startTime: Long, endTime: Long): LiveData<List<PeriodicEntry>>


    @Query("""
        SELECT :startTime as startTime, 
                SUM(totalCalories) as totalCalories, 
                SUM(totalDuration) as totalDuration, 
                SUM(totalDistance) as totalDistance, 
                SUM(totalSteps) as totalSteps 
        FROM periodicEntry 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSumSync (startTime: Long, endTime: Long): PeriodicEntry

    @Query("""
        SELECT :startTime as startTime, 
                SUM(totalCalories) as totalCalories, 
                SUM(totalDuration) as totalDuration, 
                SUM(totalDistance) as totalDistance, 
                SUM(totalSteps) as totalSteps 
        FROM periodicEntry 
        WHERE startTime >= :startTime AND startTime < :endTime
    """)
    fun getTimeRangeSumLive (startTime: Long, endTime: Long): LiveData<PeriodicEntry>
} 
