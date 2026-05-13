package com.bruhascended.fitapp.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.health.HealthConnectPermissions
import com.bruhascended.fitapp.repository.ActivityEntryRepository
import com.bruhascended.fitapp.repository.FoodEntryRepository
import com.bruhascended.fitapp.repository.PreferencesRepository
import com.bruhascended.fitapp.repository.WeightEntryRepository
import com.bruhascended.fitapp.ui.settings.SettingsActivity
import com.bruhascended.fitapp.util.enqueueSyncJob
import com.bruhascended.fitapp.health.HealthConnectSyncManager
import com.bruhascended.fitapp.reminders.MealReminderScheduler
import com.bruhascended.fitapp.reminders.WeightReminderScheduler
import com.bruhascended.fitapp.workers.UpdateUserWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

class MainActivity : AppCompatActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                MealReminderScheduler.rescheduleAll(this)
                WeightReminderScheduler.rescheduleAll(this)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                offerOpenNotificationSettingsDialog()
            }
        }

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var fabPresenter: FabPresenter
    private lateinit var repo: PreferencesRepository
    private lateinit var foodEntryRepository: FoodEntryRepository
    private lateinit var activityEntryRepository: ActivityEntryRepository
    private lateinit var weightEntryRepository: WeightEntryRepository

    private val healthPermissionLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(HealthConnectPermissions.readPermissions)) {
                syncHealthConnectData()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodEntryRepository = FoodEntryRepository.Delegate(application).getValue(this, ::foodEntryRepository)
        activityEntryRepository =
            ActivityEntryRepository.Delegate(application).getValue(this, ::activityEntryRepository)
        weightEntryRepository =
            WeightEntryRepository.Delegate(application).getValue(this, ::weightEntryRepository)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController
        setupSmoothBottomMenu()

        fabPresenter = FabPresenter(this, binding)
        fabPresenter.setupFABs()

        immediateSync()
        if (savedInstanceState == null) {
            binding.root.post {
                offerHealthConnectConnection(userInvoked = false)
            }
            lifecycleScope.launch {
                delay(750)
                if (!isFinishing && !isDestroyed) {
                    mealRemindersKickoff()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MealReminderScheduler.rescheduleAll(this)
        WeightReminderScheduler.rescheduleAll(this)
        // Never launch the permission sheet from onResume — each return from the flow
        // would fire again and the system stacks GrantPermissionsActivity until MainActivity
        // is force-finished (see logcat: ActivityTaskManager force remove).
        syncHealthConnectDataIfPermitted()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.bottom_menu_items)
        val menu = popupMenu.menu
        binding.bottomNav.setupWithNavController(menu, navController)
    }

    private fun immediateSync() {
        repo = PreferencesRepository(this)
        if (FirebaseAuth.getInstance().currentUser != null) {
            enqueueSyncJob(this, UpdateUserWorker.WORK_NAME)
        }
    }

    private fun mealRemindersKickoff() {
        MealReminderScheduler.rescheduleAll(this)
        WeightReminderScheduler.rescheduleAll(this)
        promptNotificationPermissionIfNeeded()
    }

    private fun promptNotificationPermissionIfNeeded() {
        if (!canShowHealthConnectUi()) return
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            MealReminderScheduler.rescheduleAll(this)
            WeightReminderScheduler.rescheduleAll(this)
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.notifications_permission_title)
            .setMessage(R.string.notifications_permission_message)
            .setNegativeButton(R.string.notifications_permission_not_now, null)
            .setPositiveButton(R.string.notifications_permission_allow) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    openAppNotificationSettings()
                }
            }
            .show()
    }

    private fun offerOpenNotificationSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.notifications_open_settings_title)
            .setMessage(R.string.notifications_open_settings_message)
            .setNegativeButton(R.string.notifications_permission_not_now, null)
            .setPositiveButton(R.string.notifications_open_settings_button) { _, _ ->
                openAppNotificationSettings()
            }
            .show()
    }

    private fun openAppNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            runCatching { startActivity(intent) }.onFailure { openApplicationDetailsFallback() }
        } else {
            openApplicationDetailsFallback()
        }
    }

    private fun openApplicationDetailsFallback() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            },
        )
    }

    /**
     * Shows an in-app dialog before launching the system Health Connect permission flow.
     * @param userInvoked false on cold start (skips "unavailable" nag); true from overflow menu.
     */
    private fun offerHealthConnectConnection(userInvoked: Boolean = false) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            when (HealthConnectClient.getSdkStatus(this@MainActivity)) {
                HealthConnectClient.SDK_UNAVAILABLE ->
                    if (userInvoked && canShowHealthConnectUi()) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle(R.string.health_connect_dialog_title)
                            .setMessage(R.string.health_connect_unavailable_message)
                            .setPositiveButton(R.string.ok, null)
                            .show()
                    } else {
                        Log.w(TAG, "Health Connect not supported on this device or profile")
                    }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                    if (canShowHealthConnectUi()) {
                        showHealthConnectProviderUpdateDialog()
                    } else {
                        Log.w(TAG, "Health Connect provider update required")
                    }
                HealthConnectClient.SDK_AVAILABLE ->
                    try {
                        val client = HealthConnectClient.getOrCreate(this@MainActivity)
                        val granted = client.permissionController.getGrantedPermissions()
                        when {
                            granted.containsAll(HealthConnectPermissions.allPermissions) ->
                                syncHealthConnectData()

                            !granted.containsAll(HealthConnectPermissions.readPermissions) && canShowHealthConnectUi() ->
                                showConnectHealthConnectPermissionDialog()

                            granted.containsAll(HealthConnectPermissions.readPermissions) &&
                                !granted.containsAll(HealthConnectPermissions.writePermissions) &&
                                canShowHealthConnectUi() ->
                                showHealthConnectWriteNutritionDialog()

                            else -> Unit
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Health Connect setup failed", e)
                    }
            }
        }
    }

    private fun canShowHealthConnectUi(): Boolean = !isFinishing && !isDestroyed

    private fun showConnectHealthConnectPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.health_connect_dialog_title)
            .setMessage(R.string.health_connect_dialog_message)
            .setNegativeButton(R.string.health_connect_not_now, null)
            .setPositiveButton(R.string.health_connect_connect) { d, _ ->
                d.dismiss()
                val perms = HealthConnectPermissions.allPermissions
                Log.i(TAG, "Health Connect launch: requesting ${perms.size} permission(s) (read + nutrition write)")
                window.decorView.post {
                    if (!isFinishing && !isDestroyed) {
                        try {
                            healthPermissionLauncher.launch(perms)
                        } catch (e: Exception) {
                            Log.e(TAG, "Health Connect permission launch failed", e)
                        }
                    }
                }
            }
            .show()
    }

    private fun showHealthConnectWriteNutritionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.health_connect_write_dialog_title)
            .setMessage(R.string.health_connect_write_dialog_message)
            .setNegativeButton(R.string.health_connect_not_now, null)
            .setPositiveButton(R.string.health_connect_connect) { d, _ ->
                d.dismiss()
                val perms = HealthConnectPermissions.allPermissions
                Log.i(TAG, "Health Connect launch (add write): full read+write sheet (${perms.size} permission(s))")
                window.decorView.post {
                    if (!isFinishing && !isDestroyed) {
                        try {
                            healthPermissionLauncher.launch(perms)
                        } catch (e: Exception) {
                            Log.e(TAG, "Health Connect permission launch failed", e)
                        }
                    }
                }
            }
            .show()
    }

    private fun showHealthConnectProviderUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.health_connect_update_required_title)
            .setMessage(R.string.health_connect_update_required_message)
            .setNegativeButton(R.string.health_connect_not_now, null)
            .setPositiveButton(R.string.health_connect_open_health_connect) { _, _ ->
                try {
                    startActivity(HealthConnectClient.getHealthConnectManageDataIntent(this))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, R.string.health_connect_open_failed, Toast.LENGTH_LONG).show()
                }
            }
            .show()
    }

    private fun syncHealthConnectDataIfPermitted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            if (HealthConnectClient.getSdkStatus(this@MainActivity) != HealthConnectClient.SDK_AVAILABLE) {
                return@launch
            }
            try {
                val client = HealthConnectClient.getOrCreate(this@MainActivity)
                val granted = client.permissionController.getGrantedPermissions()
                if (granted.containsAll(HealthConnectPermissions.readPermissions)) {
                    syncHealthConnectData()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Health Connect not available for sync", e)
            }
        }
    }

    private fun syncHealthConnectData() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        lifecycleScope.launch {
            val client = try {
                HealthConnectClient.getOrCreate(this@MainActivity)
            } catch (e: Exception) {
                Log.e(TAG, "Health Connect client unavailable", e)
                return@launch
            }
            try {
                HealthConnectSyncManager(this@MainActivity).sync(client)
            } catch (e: Exception) {
                Log.e(TAG, "Health Connect sync failed", e)
            }
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
            R.id.health_connect -> {
                offerHealthConnectConnection(userInvoked = true)
                return true
            }
            R.id.settings_activity -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
