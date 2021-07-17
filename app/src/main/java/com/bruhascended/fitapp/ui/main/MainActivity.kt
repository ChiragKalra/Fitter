package com.bruhascended.fitapp.ui.main

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType

enum class FitActionsRequestCode {
    READ_SESSIONS,
    INSERT_SESSIONS
}

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fabPresenter: FabPresenter
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var requestAndroidPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var requestOauthPermissionsLauncher: ActivityResultLauncher<Intent>
    private val fitnessOptions by lazy {
        FitnessOptions.builder()
            .accessActivitySessions(FitActionsRequestCode.READ_SESSIONS.ordinal)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY, FitnessOptions.ACCESS_READ)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            checkForPermissions()
    }

    private fun setUpResultContracts() {
        requestAndroidPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                var allImpPermissionsApproved = true
                for (permission in permissions.keys) {
                    if (permissions[permission] == false) {
                        Toast.makeText(this, "Imp android permissions denied", Toast.LENGTH_SHORT)
                            .show()
                        allImpPermissionsApproved = false
                        break
                    }
                }
                if (allImpPermissionsApproved) checkForOauthPermissions()
            }

        requestOauthPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) performFitActions()
                else
                    Toast.makeText(this, "Oauth Permissions Denied", Toast.LENGTH_SHORT).show()
            }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkForPermissions() {
        when {
            checkSelfPermission(
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkForOauthPermissions()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) -> {
                Toast.makeText(
                    this,
                    "Imp Android Permission already denied by user",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

            else -> {
                requestAndroidPermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.ACTIVITY_RECOGNITION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkForOauthPermissions() {
        if (GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)) {
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

    private fun performFitActions() {
        viewModel.apply {
            syncPassiveData(this@MainActivity, getGoogleAccount())
            syncActivities(this@MainActivity, getGoogleAccount())
        }
    }

    private fun getGoogleAccount(): GoogleSignInAccount {
        return GoogleSignIn.getAccountForExtension(this, fitnessOptions)
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
