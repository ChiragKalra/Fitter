package com.bruhascended.fitapp.ui.main

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.Menu
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
import com.bruhascended.fitapp.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

enum class permissions(val str: String) {
    FOREGROUND_LOCATION(android.Manifest.permission.ACCESS_FINE_LOCATION),
    ACTIVITY_RECOGNITION(android.Manifest.permission.ACTIVITY_RECOGNITION)
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fabPresenter: FabPresenter
    private lateinit var viewModel: MainActivityViewModel
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
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
        checkActivityRecognitionPermission()
    }

    private fun checkAndroidRuntimePermissions() {
        if (isAndroidRunTimePermissionGiven(this, permissions.FOREGROUND_LOCATION.str)) {
            checkForOauthPermissions()
        } else {
            requestAndroidPermissionLauncher.launch(
                arrayOf(permissions.FOREGROUND_LOCATION.str)
            )
        }
    }

    private fun checkActivityRecognitionPermission() {
        if (isActivityRecognitionPermissionGranted(this)) {
            checkAndroidRuntimePermissions()
        } else {
            requestAndroidPermissionLauncher.launch(
                arrayOf(permissions.ACTIVITY_RECOGNITION.str)
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
                val permission = it.keys.toList()
                if (runningQOrLater) {
                    if (permission[0] == permissions.ACTIVITY_RECOGNITION.str) {
                        if (it[permission[0]] == true) {
                            checkAndroidRuntimePermissions()
                        } else {
                            // TODO here core android permission activity recognition is denied
                        }
                    } else checkIfPermissionGranted(it[permission[0]])
                } else {
                    checkIfPermissionGranted(it[permission[0]])
                }
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

    private fun checkIfPermissionGranted(granted: Boolean?) {
        if (granted != null) {
            if (granted) checkForOauthPermissions()
            else {
                // TODO here core android permission location is denied
            }
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
}