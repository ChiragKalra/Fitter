package com.bruhascended.fitapp.ui.health

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bruhascended.fitapp.databinding.ActivityHealthConnectPermissionRationaleBinding

/**
 * Shown when the user (or Health Connect settings) opens the app’s permission rationale.
 * Required on Android 14+ so [com.google.android.healthconnect.controller] does not immediately
 * finish with “App should support rationale intent, finishing!”.
 */
class HealthConnectPermissionRationaleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityHealthConnectPermissionRationaleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.rationaleDone.setOnClickListener { finish() }
    }
}
