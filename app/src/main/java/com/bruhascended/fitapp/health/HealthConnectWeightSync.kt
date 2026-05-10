package com.bruhascended.fitapp.health

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.db.weight.types.WeightType
import java.time.Instant
import java.time.temporal.ChronoUnit

object HealthConnectWeightSync {

    private const val TAG = "HealthConnectWeight"

    suspend fun importWeights(client: HealthConnectClient): List<WeightEntry> {
        val end = Instant.now()
        val start = end.minus(HealthConnectNutritionSync.DAYS_BACK, ChronoUnit.DAYS)
        val range = TimeRangeFilter.between(start, end)
        val records = client.readRecords(
            ReadRecordsRequest(WeightRecord::class, timeRangeFilter = range)
        ).records
        val entries = records.map { r ->
            WeightEntry(
                weight = r.weight.inKilograms,
                type = WeightType.Kilogram,
                timeInMillis = r.time.toEpochMilli(),
                id = 0L,
                hcId = r.metadata.id,
            )
        }.sortedBy { it.timeInMillis }
        Log.i(TAG, "import ${entries.size} weight samples")
        return entries
    }

    fun mapSingleWeight(r: WeightRecord): WeightEntry {
        return WeightEntry(
            weight = r.weight.inKilograms,
            type = WeightType.Kilogram,
            timeInMillis = r.time.toEpochMilli(),
            id = 0L,
            hcId = r.metadata.id,
        )
    }
}
