package com.bruhascended.fitapp.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.SettingsDataStore
import com.bruhascended.fitapp.repository.UserPreferenceRepository
import com.bruhascended.fitapp.ui.main.permissions
import com.bruhascended.fitapp.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        setSupportActionBar(findViewById(R.id.settings_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private lateinit var signInSummaryProvider: Preference.SummaryProvider<Preference>
        private lateinit var userPreferenceRepository: UserPreferenceRepository

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            // setUp google fit
            setUpResultContracts()
            userPreferenceRepository = UserPreferenceRepository(requireContext())
            findPreference<Preference>("signInStatus")?.setOnPreferenceClickListener {
                checkAndroidRuntimePermissions()
                true
            }
            signInSummaryProvider = Preference.SummaryProvider<Preference> {
                runBlocking {
                    userPreferenceRepository.userPreferencesFLow.first().signInStatus
                }
            }
            findPreference<Preference>("signInStatus")?.summaryProvider = signInSummaryProvider
            runBlocking {
                val isSignedIn =
                    userPreferenceRepository.getPreference(UserPreferenceRepository.PreferenceKeys.IS_SIGNED_IN)
                        .first() ?: false
                if (!isSignedIn) {
                    findPreference<SwitchPreferenceCompat>("syncEnabled")?.isEnabled = false
                }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.preferenceDataStore = SettingsDataStore(requireContext())
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        private fun checkAndroidRuntimePermissions() {
            val permissionsMap =
                getAndroidRunTimePermissionGivenMap(requireContext(), permissions.values().toList())
            val permissionsNeeded = mutableListOf<String>()
            for (key in permissionsMap.keys) {
                if (permissionsMap[key] == false) {
                    permissionsNeeded.add(key)
                }
            }
            if (permissionsNeeded.size == 0) {
                checkForOauthPermissions()
            } else {
                requestAndroidPermissionLauncher.launch(
                    permissionsNeeded.toTypedArray()
                )
            }
        }

        private fun checkForOauthPermissions() {
            if (isOauthPermissionsApproved(requireContext(), FitBuilder.fitnessOptions)) {
                updatePreferenceUI()
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

        private fun checkIfPermissionGranted(map: MutableMap<String, Boolean>) {
            var allApproved = true
            for (key in map.keys) {
                if (map[key] == false) {
                    allApproved = false
                    break
                    // TODO here core runtime permission(key) is denied
                }
            }
            if (allApproved) {
                checkForOauthPermissions()
            }
        }

        private fun setUpResultContracts() {
            requestAndroidPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    checkIfPermissionGranted(it)
                }

            requestOauthPermissionsLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult())
                {
                    if (it.resultCode == Activity.RESULT_OK) {
                        updatePreferenceUI()
                    } else {
                        // TODO here user's google fit authorisation/signIn has failed
                    }
                }
        }

        private fun updatePreferenceUI() {
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
            if (account != null) {
                runBlocking {
                    userPreferenceRepository.updateSignInStatus(account.email)
                }
                findPreference<Preference>("signInStatus")?.summaryProvider = signInSummaryProvider
            }
        }
    }
}