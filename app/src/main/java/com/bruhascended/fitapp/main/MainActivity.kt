package com.bruhascended.fitapp.main

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.databinding.LayoutFabsBinding
import com.bruhascended.fitapp.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fabsBinding: LayoutFabsBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate the layout and initialise dataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        fabsBinding = binding.fabsLayout

        // initialise navController
        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.fragment) as NavHostFragment
        navController = navHostFragment.navController

        // set our defined toolbar as actionbar(only needed if we want overflow options in actionbar)
        setSupportActionBar(binding.toolbar)

        //setup bottomNav with navController
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.dashboardFragment, R.id.challengesFragment, R.id.journalFragment))
        binding.bottomNav.setupWithNavController(navController)

        //setup collapsing toolbar with navController
        binding.collapsingToolbar.setupWithNavController(binding.toolbar, navController, appBarConfiguration)

        // setup FloatingActionButtons
        setupFABs()
    }

    private var fabsHidden = true
    private fun setupFABs() {
        fabsBinding.apply {
            val actionButtons = arrayOf (
                    captureFoodButton, addFoodButton, addWorkoutButton, addWeightButton
            )
            addActionButton.setOnClickListener {
                if (fabsHidden) {
                    addActionButton.animateRotation(135f)
                    backgroundView.animateFadeIn(0.9f)
                    for (actionButton in actionButtons) {
                        actionButton.animateFadeUpIn(toPx(12))
                    }
                } else {
                    addActionButton.animateRotation(0f)
                    backgroundView.animateFadeOut()
                    for (actionButton in actionButtons) {
                        actionButton.animateFadeDownOut(toPx(12))
                    }
                }
                fabsHidden = !fabsHidden
            }
        }
    }

    //over flow menu ->  to add onclick later
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu_item, menu)
        return true
    }

}