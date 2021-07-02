package com.bruhascended.db

import android.content.Context
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ApplicationProvider
import com.bruhascended.db.weight.WeightEntryDatabase
import com.bruhascended.db.weight.WeightEntryDatabaseFactory
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.db.weight.types.WeightType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.*


@RunWith(AndroidJUnit4::class)
class WeightEntryDbTest {
    private lateinit var db: WeightEntryDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = WeightEntryDatabaseFactory(context).allowMainThreadOperations(false).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun weightDbTest() {
        val weight = WeightEntry(
            70.0,
            WeightType.Kilogram,
            Calendar.getInstance().timeInMillis
        )

        // insertion test
        val id = db.entryManager().insert(weight)
        val byId = db.entryManager().findById(weight.id)
        assertEquals(weight, byId)

        // deletion test
        db.entryManager().delete(weight)
        val afterDelete = db.entryManager().findById(id)
        assertEquals(afterDelete, null)
    }

}
