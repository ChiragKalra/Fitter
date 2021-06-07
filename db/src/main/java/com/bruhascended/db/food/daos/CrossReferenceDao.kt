package com.bruhascended.db.food.daos

import androidx.room.*
import com.bruhascended.db.food.entities.CrossReference

@Dao
interface CrossReferenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert (crossReference: CrossReference)

    @Delete
    fun delete (crossReference: CrossReference)
}