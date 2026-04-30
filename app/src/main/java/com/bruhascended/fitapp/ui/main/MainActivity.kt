package com.bruhascended.fitapp.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.util.enqueueImmediateJob
import com.bruhascended.fitapp.util.getCurrentAccount
import com.bruhascended.fitapp.workers.ActivityEntryWorker
import com.bruhascended.fitapp.workers.PeriodicEntryWorker

val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fabPresenter: FabPresenter
    private lateinit var repo: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.toolbar)

        // initialise navController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController

        //setup bottomNav with navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.journalFragment,
                R.id.friendsFragment,
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
    }

    override fun onStart() {
        immediateSync()
        super.onStart()
    }

    private fun immediateSync() {
        repo = PreferencesRepository(this)
        val syncEnabled =
            repo.getPreference(PreferencesRepository.PreferencesKeys.SYNC_ENABLED).toString()
                .toBooleanStrictOrNull() ?: false
        if (getCurrentAccount(this) != null && syncEnabled) {
            enqueueImmediateJob(this, PeriodicEntryWorker.WORK_NAME)
            enqueueImmediateJob(this, ActivityEntryWorker.WORK_NAME)
        }
    }

    override fun onBackPressed() {
        if (fabPresenter.areMiniFabsVisible) fabPresenter.cancelMiniFabs()
        else super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow_menu_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings_activity -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}