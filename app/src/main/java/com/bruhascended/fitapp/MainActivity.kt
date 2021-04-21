package com.bruhascended.fitapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.bruhascended.fitapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //inflate the layout and initialise dataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        //initialise navController
        navController = findNavController(R.id.fragment)

        //set our defined toolbar as actionbar and set title of appbar to default(which is dashboard) when onCreate invoked
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "DashBoard"

        //setup bottomNav click listeners and handle the title of the app bar with fragment change
        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnNavigationItemSelectedListener {
            binding.appBar.setExpanded(true)
            when (it.itemId) {
                R.id.dashboard -> {
                    navController.navigate(R.id.dashboardFragment)
                    binding.collapsingToolbar.title = "DashBoard"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.challenges -> {
                    navController.navigate(R.id.challengesFragment)
                    binding.collapsingToolbar.title = "Challenges"
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.journal -> {
                    navController.navigate(R.id.journalFragment)
                    binding.collapsingToolbar.title = "Journal"
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