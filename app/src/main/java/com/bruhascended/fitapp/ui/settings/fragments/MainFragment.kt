package com.bruhascended.fitapp.ui.settings.fragments

import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.health.HealthConnectPermissions
import com.bruhascended.fitapp.health.HealthConnectSyncManager
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.reminders.MealReminderScheduler
import com.bruhascended.fitapp.ui.settings.MealReminderPreference
import com.bruhascended.fitapp.ui.settings.SettingsDataStore
import com.bruhascended.fitapp.util.cancelWork
import com.bruhascended.fitapp.util.enqueueImmediateJob
import com.bruhascended.fitapp.util.isWorkScheduled
import com.bruhascended.fitapp.workers.UpdateUserWorker
import com.google.android.material.timepicker.MaterialTimePicker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainFragment : PreferenceFragmentCompat() {

    companion object {
        private val MEAL_REMINDER_CARDS =
            listOf(
                MealReminderCard(
                    "REMINDER_BREAKFAST_ENABLED",
                    PreferencesKeys.REMINDER_BREAKFAST_MINUTES,
                    PreferencesRepository.DefaultMealReminders.BREAKFAST_MINUTES.toLong(),
                ),
                MealReminderCard(
                    "REMINDER_LUNCH_ENABLED",
                    PreferencesKeys.REMINDER_LUNCH_MINUTES,
                    PreferencesRepository.DefaultMealReminders.LUNCH_MINUTES.toLong(),
                ),
                MealReminderCard(
                    "REMINDER_SNACK_ENABLED",
                    PreferencesKeys.REMINDER_SNACK_MINUTES,
                    PreferencesRepository.DefaultMealReminders.SNACK_MINUTES.toLong(),
                ),
                MealReminderCard(
                    "REMINDER_DINNER_ENABLED",
                    PreferencesKeys.REMINDER_DINNER_MINUTES,
                    PreferencesRepository.DefaultMealReminders.DINNER_MINUTES.toLong(),
                ),
            )

        private data class MealReminderCard(
            val enabledKey: String,
            val minutesKey: Preferences.Key<Long>,
            val defaultMinutes: Long,
        )
    }

    private lateinit var repo: PreferencesRepository
    private var healthConnectPreference: Preference? = null
    private var syncEnabledPreference: SwitchPreferenceCompat? = null
    private var energyPref: EditTextPreference? = null
    private var stepsPref: EditTextPreference? = null
    private var durationPref: EditTextPreference? = null
    private var distancePref: EditTextPreference? = null
    private var consumedPref: EditTextPreference? = null

    private val healthPermissionLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(HealthConnectPermissions.readPermissions)) {
                syncHealthConnectNow()
            }
            refreshHealthConnectSummary()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = PreferencesRepository((requireContext()))
        healthConnectPreference = findPreference("HEALTH_CONNECT_MANAGE")
        syncEnabledPreference = findPreference("SYNC_ENABLED")
        energyPref = findPreference("GOAL_CALORIE_BURN")
        stepsPref = findPreference("GOAL_STEPS")
        durationPref = findPreference("GOAL_DURATION")
        distancePref = findPreference("GOAL_DISTANCE")
        consumedPref = findPreference("GOAL_CALORIE_CONSUMPTION")

        setUpEditPrefs()
        setUpHealthConnectPreference()
        setUpMealReminders()

        lifecycleScope.launch {
            repo.userStatsFlow.collect {
                if (!it.syncEnabled) {
                    cancelWork(requireContext(), UpdateUserWorker.WORK_NAME)
                } else {
                    if (!isWorkScheduled(requireContext(), UpdateUserWorker.WORK_NAME)) {
                        enqueueImmediateJob(requireContext(), UpdateUserWorker.WORK_NAME)
                    }
                    lifecycleScope.launch {
                        syncHealthConnectNow()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshHealthConnectSummary()
    }

    override fun onResume() {
        super.onResume()
        refreshMealReminderCards()
        refreshHealthConnectSummary()
    }

    private fun setUpHealthConnectPreference() {
        healthConnectPreference?.setOnPreferenceClickListener {
            offerHealthConnectPermissionFromSettings()
            true
        }
    }

    private fun offerHealthConnectPermissionFromSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val hostView = view ?: return
        lifecycleScope.launch {
            val ctx = requireContext()
            when (HealthConnectClient.getSdkStatus(ctx)) {
                HealthConnectClient.SDK_UNAVAILABLE ->
                    AlertDialog.Builder(ctx)
                        .setTitle(R.string.health_connect_dialog_title)
                        .setMessage(R.string.health_connect_unavailable_message)
                        .setPositiveButton(R.string.ok, null)
                        .show()

                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                    AlertDialog.Builder(ctx)
                        .setTitle(R.string.health_connect_update_required_title)
                        .setMessage(R.string.health_connect_update_required_message)
                        .setNegativeButton(R.string.health_connect_not_now, null)
                        .setPositiveButton(R.string.health_connect_open_health_connect) { _, _ ->
                            runCatching {
                                requireActivity().startActivity(HealthConnectClient.getHealthConnectManageDataIntent(ctx))
                            }.onFailure {
                                Toast.makeText(ctx, R.string.health_connect_open_failed, Toast.LENGTH_LONG).show()
                            }
                        }
                        .show()

                HealthConnectClient.SDK_AVAILABLE ->
                    try {
                        val client = HealthConnectClient.getOrCreate(ctx)
                        val granted = client.permissionController.getGrantedPermissions()
                        when {
                            granted.containsAll(HealthConnectPermissions.allPermissions) -> {
                                syncHealthConnectNow()
                                Toast.makeText(ctx, R.string.health_connect_already_connected, Toast.LENGTH_SHORT).show()
                                refreshHealthConnectSummary()
                            }
                            !granted.containsAll(HealthConnectPermissions.readPermissions) -> {
                                AlertDialog.Builder(ctx)
                                    .setTitle(R.string.health_connect_dialog_title)
                                    .setMessage(R.string.health_connect_dialog_message)
                                    .setNegativeButton(R.string.health_connect_not_now, null)
                                    .setPositiveButton(R.string.health_connect_connect) { d, _ ->
                                        d.dismiss()
                                        hostView.post {
                                            healthPermissionLauncher.launch(HealthConnectPermissions.allPermissions)
                                        }
                                    }
                                    .show()
                            }
                            else -> {
                                AlertDialog.Builder(ctx)
                                    .setTitle(R.string.health_connect_write_dialog_title)
                                    .setMessage(R.string.health_connect_write_dialog_message)
                                    .setNegativeButton(R.string.health_connect_not_now, null)
                                    .setPositiveButton(R.string.health_connect_connect) { d, _ ->
                                        d.dismiss()
                                        hostView.post {
                                            healthPermissionLauncher.launch(HealthConnectPermissions.allPermissions)
                                        }
                                    }
                                    .show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Settings", "Health Connect UI failed", e)
                    }
            }
        }
    }

    private fun syncHealthConnectNow() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            try {
                if (HealthConnectClient.getSdkStatus(requireContext()) != HealthConnectClient.SDK_AVAILABLE) {
                    return@launch
                }
                val client = HealthConnectClient.getOrCreate(requireContext())
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(HealthConnectPermissions.readPermissions)) {
                    HealthConnectSyncManager(requireContext()).sync(client)
                }
            } catch (e: Exception) {
                Log.e("Settings", "HC sync failed", e)
            }
        }
    }

    private fun refreshHealthConnectSummary() {
        val pref = healthConnectPreference ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            pref.summary = getString(R.string.health_connect_unavailable_message)
            pref.isEnabled = false
            return
        }
        pref.isEnabled = true
        lifecycleScope.launch {
            try {
                val ctx = requireContext()
                when (HealthConnectClient.getSdkStatus(ctx)) {
                    HealthConnectClient.SDK_UNAVAILABLE ->
                        pref.summary = getString(R.string.health_connect_summary_sdk_unavailable)

                    HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                        pref.summary = getString(R.string.health_connect_summary_update_required)

                    HealthConnectClient.SDK_AVAILABLE -> {
                        val client = HealthConnectClient.getOrCreate(ctx)
                        val granted = client.permissionController.getGrantedPermissions()
                        pref.summary = when {
                            granted.containsAll(HealthConnectPermissions.allPermissions) ->
                                getString(R.string.health_connect_summary_connected)

                            granted.containsAll(HealthConnectPermissions.readPermissions) ->
                                getString(R.string.health_connect_summary_read_only_import)

                            else -> getString(R.string.health_connect_summary_not_connected)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("Settings", "Health Connect summary refresh failed", e)
                pref.summary = getString(R.string.health_connect_summary_not_connected)
            }
        }
    }

    private fun setUpEditPrefs() {
        val userActivityGoals = repo.activityGoalsFlow
        val userNutrientGoals = repo.nutritionGoalsFlow

        energyPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = userActivityGoals.calories.toString()
            }
        }
        stepsPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = userActivityGoals.steps.toString()
            }
        }
        durationPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = userActivityGoals.duration.toString()
            }
        }
        distancePref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = userActivityGoals.distance.toString()
            }
        }
        consumedPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = userNutrientGoals.calories.toString()
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = SettingsDataStore(requireContext())
        setPreferencesFromResource(R.xml.main_fragment_preferences, rootKey)
    }

    private fun setUpMealReminders() {
        MEAL_REMINDER_CARDS.forEach { meal ->
            findPreference<MealReminderPreference>(meal.enabledKey)?.apply {
                onPickTime = {
                    pickMealTime(this, meal.minutesKey, meal.defaultMinutes)
                }
                setOnPreferenceChangeListener { _, _ ->
                    MealReminderScheduler.rescheduleAll(requireContext().applicationContext)
                    true
                }
            }
        }
        refreshMealReminderCards()
    }

    private fun pickMealTime(
        pref: MealReminderPreference,
        minutesKey: Preferences.Key<Long>,
        defaultMinutes: Long,
    ) {
        val stored = repo.getPreference(minutesKey) as? Long
        val current = MealReminderScheduler.clampMinutes(stored ?: defaultMinutes)
        val picker = MaterialTimePicker.Builder()
            .setHour((current / 60L).toInt())
            .setMinute((current % 60L).toInt())
            .setTitleText(resources.getText(pref.pickerTitle()))
            .build()
        picker.addOnPositiveButtonClickListener {
            val minutes = picker.hour * 60 + picker.minute
            val clamped = MealReminderScheduler.clampMinutes(minutes.toLong())
            repo.updatePreference(minutesKey, clamped.toLong())
            MealReminderScheduler.rescheduleAll(requireContext().applicationContext)
            pref.requestRebind()
        }
        picker.show(parentFragmentManager, "meal_time_${minutesKey.name}")
    }

    private fun refreshMealReminderCards() {
        MEAL_REMINDER_CARDS.forEach { meal ->
            findPreference<MealReminderPreference>(meal.enabledKey)?.requestRebind()
        }
    }
}
