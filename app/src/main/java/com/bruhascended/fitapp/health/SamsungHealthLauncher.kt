package com.bruhascended.fitapp.health

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.bruhascended.fitapp.R

/**
 * Best-effort deep link / package launch for Samsung Health food logging.
 * URIs and components can change with One UI updates; fall back to app launch or Play Store.
 */
object SamsungHealthLauncher {

    private const val TAG = "SamsungHealthLauncher"
    const val PACKAGE_NAME = "com.sec.android.app.shealth"

    fun openLogFood(activity: Activity) {
        val pm = activity.packageManager
        val attempts = listOf(
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("shealth://food")
                `package` = PACKAGE_NAME
            },
            Intent(Intent.ACTION_VIEW, Uri.parse("shealth://main?item=food")).apply {
                `package` = PACKAGE_NAME
            },
            pm.getLaunchIntentForPackage(PACKAGE_NAME)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
        for (intent in attempts) {
            if (intent != null && intent.resolveActivity(pm) != null) {
                try {
                    activity.startActivity(intent)
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "Intent failed: $intent", e)
                }
            }
        }
        Toast.makeText(
            activity,
            R.string.samsung_health_open_failed,
            Toast.LENGTH_LONG
        ).show()
        try {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME")
                )
            )
        } catch (_: ActivityNotFoundException) {
            // ignore
        }
    }
}
