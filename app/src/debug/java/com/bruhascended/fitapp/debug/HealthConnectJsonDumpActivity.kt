package com.bruhascended.fitapp.debug

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.lifecycleScope
import com.bruhascended.fitapp.health.HealthConnectDayDump
import com.bruhascended.fitapp.health.HealthConnectPermissions
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException

/**
 * Debug-only. Must run in the foreground so Health Connect allows reading other apps’ data (e.g. Samsung).
 *
 * adb shell am start -n com.bruhascended.fitapp/com.bruhascended.fitapp.debug.HealthConnectJsonDumpActivity \
 *   --es date 2026-04-16 --es zoneId Europe/London
 *
 * adb pull "/sdcard/Android/data/com.bruhascended.fitapp/files/health_connect_exports/health_connect_dump_2026-04-16.json"
 */
class HealthConnectJsonDumpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
        window.setDecorFitsSystemWindows(true)

        val rawDate = intent.getStringExtra(EXTRA_DATE) ?: "2026-04-16"
        val rawZone = intent.getStringExtra(EXTRA_ZONE_ID)
        val localDate =
            try {
                LocalDate.parse(rawDate)
            } catch (_: DateTimeParseException) {
                toastAndFinish("Bad date format, use yyyy-MM-DD (got $rawDate)")
                return
            }
        val zoneId =
            try {
                if (rawZone.isNullOrBlank()) ZoneId.systemDefault() else ZoneId.of(rawZone)
            } catch (_: Exception) {
                toastAndFinish("Unknown zoneId: $rawZone")
                return
            }

        lifecycleScope.launch {
            try {
                when (HealthConnectClient.getSdkStatus(this@HealthConnectJsonDumpActivity)) {
                    HealthConnectClient.SDK_UNAVAILABLE -> {
                        toastAndFinish("Health Connect unavailable on this device")
                        return@launch
                    }
                    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                        toastAndFinish("Health Connect needs an update")
                        return@launch
                    }
                }
                val client = HealthConnectClient.getOrCreate(this@HealthConnectJsonDumpActivity)
                val granted =
                    try {
                        client.permissionController.getGrantedPermissions()
                    } catch (e: Exception) {
                        toastAndFinish("Could not read HC permissions: ${e.message}")
                        return@launch
                    }
                if (!granted.containsAll(HealthConnectPermissions.readPermissions)) {
                    toastAndFinish("Grant Fitter Health Connect read permissions in Settings first")
                    return@launch
                }
                val file =
                    HealthConnectDayDump.writeJsonForDay(
                        applicationContext,
                        client,
                        localDate,
                        zoneId,
                    )
                val msg = "${file.absolutePath} (${file.length()} bytes)"
                Log.i(TAG, msg)
                Toast.makeText(
                    this@HealthConnectJsonDumpActivity,
                    "HC dump OK\n$msg",
                    Toast.LENGTH_LONG,
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Dump failed", e)
                Toast.makeText(
                    this@HealthConnectJsonDumpActivity,
                    "Dump failed: ${e.message}",
                    Toast.LENGTH_LONG,
                ).show()
            } finally {
                finish()
            }
        }
    }

    private fun toastAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.w(TAG, message)
        finish()
    }

    companion object {
        private const val TAG = "HealthConnectJsonDump"
        const val EXTRA_DATE = "date"
        const val EXTRA_ZONE_ID = "zoneId"
    }
}
