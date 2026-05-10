package com.bruhascended.fitapp.ui.settings.fragments

import android.app.Activity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.settings.SettingsDataStore
import com.bruhascended.fitapp.util.*
import com.bruhascended.fitapp.workers.UpdateUserWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.bruhascended.fitapp.health.HealthConnectSyncManager
import androidx.health.connect.client.HealthConnectClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainFragment : PreferenceFragmentCompat() {
    private lateinit var repo: PreferencesRepository
    private var signInPreference: Preference? = null
    private var syncEnabledPreference: SwitchPreferenceCompat? = null
    private var energyPref: EditTextPreference? = null
    private var stepsPref: EditTextPreference? = null
    private var durationPref: EditTextPreference? = null
    private var distancePref: EditTextPreference? = null
    private var consumedPref: EditTextPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = PreferencesRepository((requireContext()))
        signInPreference = findPreference("SIGNED_IN")
        syncEnabledPreference = findPreference("SYNC_ENABLED")
        energyPref = findPreference("GOAL_CALORIE_BURN")
        stepsPref = findPreference("GOAL_STEPS")
        durationPref = findPreference("GOAL_DURATION")
        distancePref = findPreference("GOAL_DISTANCE")
        consumedPref = findPreference("GOAL_CALORIE_CONSUMPTION")

        setUpEditPrefs()
        setUpResultLaunchers()
        setUpSignInPreference()
        setUpSyncEnabled()

        lifecycleScope.launch {
            repo.userStatsFlow.collect {
                if (!it.syncEnabled) {
                    // cancel all sync
                    cancelWork(requireContext(), UpdateUserWorker.WORK_NAME)
                } else {
                    // do immediate sync
                    if (!isWorkScheduled(requireContext(), UpdateUserWorker.WORK_NAME))
                        enqueueImmediateJob(requireContext(), UpdateUserWorker.WORK_NAME)
                    
                    // Trigger HC incremental sync immediately
                    lifecycleScope.launch {
                        try {
                            val client = HealthConnectClient.getOrCreate(requireContext())
                            HealthConnectSyncManager(requireContext()).sync(client)
                        } catch (e: Exception) {
                            Log.e("Settings", "HC sync failed", e)
                        }
                    }
                }
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

    private fun setUpSignInPreference() {
        setUpEmail()
        signInPreference?.setOnPreferenceClickListener {
            if (getCurrentAccount(requireContext()) == null) {
                checkAndroidRunTimePermissions()
            } else {
                GoogleSignIn.getClient(
                    requireContext(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut()
                setUpSyncEnabled()
                setUpEmail()
            }
            true
        }
    }

    private fun setUpSyncEnabled() {
        val account = getCurrentAccount(requireContext())
        if (account == null) syncEnabledPreference?.apply {
            isEnabled = false
            if (isChecked) {
                isChecked = false
            }
        }
    }

    private fun checkAndroidRunTimePermissions() {
        val permissionMap = getAndroidRunTimePermissionGivenMap(requireContext())
        val permissionsList = mutableListOf<String>()
        for (key in permissionMap.keys) {
            if (!permissionMap[key]!!) {
                permissionsList.add(key)
            }
        }
        if (permissionsList.size == 0) {
            checkOauthPermissions()
        } else {
            requestAndroidPermissionLauncher.launch(permissionsList.toTypedArray())
        }
    }

    private fun setUpResultLaunchers() {
        requestAndroidPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.containsValue(false)) {
                    // todo core android runtime permissions denied
                } else {
                    checkOauthPermissions()
                }
            }
        requestOauthPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    setUpSyncAndPreferences()
                } else {
                    // todo here flow was interrupted
                    Log.e("Epic", it.resultCode.toString())
                }
            }
    }

    private fun setUpSyncAndPreferences() {
        syncEnabledPreference?.apply {
            isEnabled = true
            isChecked = true
        }
        setUpEmail()
    }

    private fun setUpEmail() {
        val account = getCurrentAccount(requireContext())
        if (account != null) signInPreference?.summary = account.email ?: ""
        else signInPreference?.summary = ""
    }

    private fun checkOauthPermissions() {
        requestOauthPermissionsLauncher.launch(
            GoogleSignIn.getClient(
                requireContext(), GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
            ).signInIntent
        )
    }
}