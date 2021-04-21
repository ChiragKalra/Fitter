package com.bruhascended.fitapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bruhascended.fitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inflate the layout and initialise dataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //initialise navController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController

        //set our defined toolbar as actionbar and set title of appbar to default(which is dashboard) when onCreate invoked
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "DashBoard"

        //setup bottomNav click listeners and handle the title of the app bar with fragment change
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> {
                    binding.appBar.setExpanded(true, true)
                    navController.navigate(R.id.dashboardFragment)
                    binding.collapsingToolbar.title = getString(R.string.dashboard)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.challenges -> {
                    binding.appBar.setExpanded(false, true)
                    navController.navigate(R.id.challengesFragment)
                    binding.collapsingToolbar.title = getString(R.string.challenges)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.journal -> {
                    binding.appBar.setExpanded(false, true)
                    navController.navigate(R.id.journalFragment)
                    binding.collapsingToolbar.title = getString(R.string.journal)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        binding.bottomNav.setOnNavigationItemReselectedListener {
            false
        }
    }

    //over flow menu ->  to add onclick later
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu_item, menu)
        return true
    }

}