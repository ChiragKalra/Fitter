package com.bruhascended.fitapp.ui.main

import android.app.Activity
import android.content.Intent
import android.widget.TextView
import androidx.core.view.isVisible
import com.bruhascended.fitapp.databinding.ActivityMainBinding
import com.bruhascended.fitapp.ui.addFood.FoodSearchActivity
import com.bruhascended.fitapp.ui.capturefood.CaptureFoodActivity
import com.bruhascended.fitapp.ui.addfriends.AddFriendsActivity
import com.bruhascended.fitapp.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class FabPresenter(
    private val mActivity: Activity,
    private val binding: ActivityMainBinding
) {

    data class FabInfo(
        val destination: Class<*>,
        val buttonView: FloatingActionButton,
        val descriptionView: TextView,
    )

    var areMiniFabsVisible = false

    // onClick destinations
    private var fabDetails: MutableSet<FabInfo>
    private var addFriendsFab: FabInfo

    init {
        binding.fabsLayout.apply {
            fabDetails = mutableSetOf(
                FabInfo(
                    CaptureFoodActivity::class.java,
                    captureFoodButton,
                    textCapture,
                ),
                FabInfo(
                    FoodSearchActivity::class.java,
                    addFoodButton,
                    textFood,
                )
            )
            addFriendsFab = FabInfo(
                    AddFriendsActivity::class.java,
                    addFriendsButton,
                    textFriends,
                )
            fabDetails.add(addFriendsFab)
        }
    }

    fun setupFABs() {
        hideMiniFabs()
        setupIntents()
        setupEntryAndExit()
    }

    fun cancelMiniFabs() = binding.fabsLayout.cancelActionButton.callOnClick()

    private fun hideMiniFabs() {
        fabDetails.forEach {
            it.buttonView.isVisible = false
            it.buttonView.alpha = 0f
            it.descriptionView.isVisible = false
            it.descriptionView.alpha = 0f
        }
        binding.fabsLayout.backgroundView.also {
            it.isVisible = false
            it.alpha = 0f
        }
        binding.fabsLayout.cancelActionButton.also {
            it.isVisible = false
            it.alpha = 0f
        }
        binding.fabsLayout.root.isVisible = true
    }

    private fun setupIntents() {
        // attach destination intents to FABs
        for (fabInfo in fabDetails) {
            fabInfo.buttonView.setOnClickListener {
                mActivity.startActivity(Intent(mActivity, fabInfo.destination))
                binding.fabsLayout.cancelActionButton.callOnClick()
            }
        }
    }

    private fun toggleAddFriendsFab() {
        if (FirebaseAuth.getInstance().uid != null) {
            fabDetails.add(addFriendsFab)
        } else {
            fabDetails.remove(addFriendsFab)
            addFriendsFab.buttonView.isVisible = false
            addFriendsFab.descriptionView.isVisible = false
        }
    }

    private fun setupEntryAndExit() {
        binding.fabsLayout.apply {
            // on show FAB buttons
            binding.addActionButton.setOnClickListener {
                toggleAddFriendsFab()
                areMiniFabsVisible = true
                binding.addActionButton.animateRotation(135f).animateFadeOut()
                cancelActionButton.animateRotation(135f).animateFadeIn(1f)
                backgroundView.animateFadeIn(0.975f)
                for (fabInfo in fabDetails) {
                    fabInfo.buttonView.animateFadeUpIn(mActivity.toPxFloat(12))
                    fabInfo.descriptionView.animateFadeUpIn(mActivity.toPxFloat(12))
                }
            }
            // on hide fab buttons
            cancelActionButton.setOnClickListener {
                areMiniFabsVisible = false
                binding.addActionButton.animateRotation(0f).animateFadeIn(1f)
                cancelActionButton.animateRotation(0f).animateFadeOut()
                backgroundView.animateFadeOut()
                for (fabInfo in fabDetails) {
                    fabInfo.buttonView.animateFadeDownOut(mActivity.toPxFloat(12))
                    fabInfo.descriptionView.animateFadeDownOut(mActivity.toPxFloat(12))
                }
            }
            // hide FAB buttons on tap outside
            backgroundView.setOnClickListener {
                cancelMiniFabs()
            }
        }
    }
}
