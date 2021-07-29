package com.bruhascended.fitapp.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.ui.SettingsActivity
import com.bruhascended.fitapp.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

enum class permissions(val str: String) {
    FOREGROUND_LOCATION(android.Manifest.permission.ACCESS_FINE_LOCATION),
    BACKGROUND_LOCATION(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),
    ACTIVITY_RECOGNITION(android.Manifest.permission.ACTIVITY_RECOGNITION)
}

val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fabPresenter: FabPresenter
    private lateinit var viewModel: MainActivityViewModel
    private val fitnessOptions by lazy {
        FitnessOptions.builder()
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        // initialise navController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController

        //setup bottomNav with navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.challengesFragment,
                R.id.journalFragment
            )
        )
        binding.bottomNav.setupWithNavController(navController)

        //setup collapsing toolbar with navController
        binding.collapsingToolbar.setupWithNavController(
            binding.toolbar,
            navController,
            appBarConfiguration
        )

        // setup FloatingActionButtons
        fabPresenter = FabPresenter(this, binding)
        fabPresenter.setupFABs()

        setUpResultContracts()
        checkAndroidRuntimePermissions()
    }

    private fun checkAndroidRuntimePermissions() {
        val permissionsMap = getAndroidRunTimePermissionGivenMap(this, permissions.values().toList())
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
        if (isOauthPermissionsApproved(this, fitnessOptions)) {
            performFitActions()
        } else {
            requestOauthPermissionsLauncher.launch(
                GoogleSignIn.getClient(
                    this, GoogleSignInOptions.Builder()
                        .addExtension(fitnessOptions)
                        .build()
                ).signInIntent
            )
        }
    }

    private fun setUpResultContracts() {
        requestAndroidPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                Log.d("eyo","${it}")
                checkIfPermissionGranted(it)
            }

        requestOauthPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            {
                if (it.resultCode == Activity.RESULT_OK) {
                    performFitActions()
                } else {
                    // TODO here user's google fit authorisation/signIn has failed
                }
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

    private fun performFitActions() {
        viewModel.apply {
            syncPassiveData(this@MainActivity, getGoogleAccount(this@MainActivity, fitnessOptions))
            syncActivities(this@MainActivity, getGoogleAccount(this@MainActivity, fitnessOptions))
        }
    }

    override fun onBackPressed() {
        if (fabPresenter.areMiniFabsVisible) fabPresenter.cancelMiniFabs()
        else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.settings_activity -> {
                val intent = Intent(this,SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}