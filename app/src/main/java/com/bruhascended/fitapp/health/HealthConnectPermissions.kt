package com.bruhascended.fitapp.health

import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord

/** Health Connect read/write scopes used by Fitter. */
object HealthConnectPermissions {

    val readPermissions: Set<String> = linkedSetOf(
        HealthPermission.getReadPermission(NutritionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
    )

    val writePermissions: Set<String> = linkedSetOf(
        HealthPermission.getWritePermission(NutritionRecord::class),
    )

    /**
     * Always pass this to [androidx.health.connect.client.PermissionController] when opening the
     * system sheet. Requesting [writePermissions] alone can show only Nutrition on some devices
     * and is easy to misread as dropping activity/weight access even though [readPermissions] are
     * still declared in the manifest.
     */
    val allPermissions: Set<String> = readPermissions + writePermissions

    /** Every entry here needs matching `READ_*` or `WRITE_*` in AndroidManifest.xml. */
}
