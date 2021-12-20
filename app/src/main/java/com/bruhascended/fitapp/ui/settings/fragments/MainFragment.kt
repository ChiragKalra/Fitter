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
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.settings.SettingsDataStore
import com.bruhascended.fitapp.util.*
import com.bruhascended.fitapp.workers.ActivityEntryWorker
import com.bruhascended.fitapp.workers.PeriodicEntryWorker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = PreferencesRepository((requireContext()))
        signInPreference = findPreference("SIGNED_IN")
        syncEnabledPreference = findPreference("SYNC_ENABLED")
        energyPref = findPreference("GOAL_CALORIE_BURN")
        stepsPref = findPreference("GOAL_STEPS")
        durationPref = findPreference("GOAL_DURATION")
        distancePref = findPreference("GOAL_DISTANCE")

        setUpEditPrefs()
        setUpResultLaunchers()
        setUpSignInPreference()
        setUpSyncEnabled()

        lifecycleScope.launch {
            repo.userStatsFlow.collect {
                if (!it.syncEnabled) {
                    // cancel all sync
                    cancelWork(requireContext(), PeriodicEntryWorker.WORK_NAME)
                    cancelWork(requireContext(), ActivityEntryWorker.WORK_NAME)
                } else {
                    // do immediate sync
                    if (!isWorkScheduled(requireContext(), PeriodicEntryWorker.WORK_NAME))
                        enqueueImmediateJob(requireContext(), PeriodicEntryWorker.WORK_NAME)
                    if (!isWorkScheduled(requireContext(), ActivityEntryWorker.WORK_NAME))
                        enqueueImmediateJob(requireContext(), ActivityEntryWorker.WORK_NAME)
                }
            }
        }
    }

    private fun setUpEditPrefs() {
        energyPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = repo.getPreference(PreferencesKeys.GOAL_CALORIE_BURN).toString()
            }
        }
        stepsPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = repo.getPreference(PreferencesKeys.GOAL_STEPS).toString()
            }
        }
        durationPref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = repo.getPreference(PreferencesKeys.GOAL_DURATION).toString()
            }
        }
        distancePref?.setOnBindEditTextListener {
            it.apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                setText("")
                hint = repo.getPreference(PreferencesKeys.GOAL_DISTANCE).toString()
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
                signOut(requireContext())
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
        if (isOauthPermissionsApproved(requireContext(), FitBuilder.fitnessOptions)) {
            // todo perform fit actions
        } else {
            requestOauthPermissionsLauncher.launch(
                GoogleSignIn.getClient(
                    requireContext(), GoogleSignInOptions.Builder()
                        .addExtension(FitBuilder.fitnessOptions)
                        .requestEmail()
                        .build()
                ).signInIntent
            )
        }
    }
}