package com.bruhascended.fitapp.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.repository.PreferencesKeys
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

        // initialise navController

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupSmoothBottomMenu()

        // setup FloatingActionButtons
        fabPresenter = FabPresenter(this, binding)
        fabPresenter.setupFABs()

        immediateSync()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this,null)
        popupMenu.inflate(R.menu.bottom_menu_items)
        val menu = popupMenu.menu
        binding.bottomNav.setupWithNavController(menu, navController)
    }

    private fun immediateSync() {
        repo = PreferencesRepository(this)
        val syncEnabled =
            repo.getPreference(PreferencesKeys.SYNC_ENABLED).toString()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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