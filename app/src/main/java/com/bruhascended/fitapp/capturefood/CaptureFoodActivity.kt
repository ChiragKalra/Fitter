package com.bruhascended.fitapp.capturefood

import android.os.Bundle
import androidx.camera.view.PreviewView
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityCaptureFoodBinding
import com.bruhascended.fitapp.util.setupToolbar


class CaptureFoodActivity : CameraActivity() {

    private lateinit var binding: ActivityCaptureFoodBinding
    private lateinit var guessPresenter: GuessPresenter

    override val cameraViewFinder: PreviewView
        get() = binding.viewFinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture_food)
        setupToolbar(binding.toolbar, mTitle = "", home = true)

        requestCameraPermissions()

        guessPresenter = GuessPresenter(this, binding.guessRecyclerView)
    }
}
