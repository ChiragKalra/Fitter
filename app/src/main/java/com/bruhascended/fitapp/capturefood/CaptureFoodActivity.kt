package com.bruhascended.fitapp.capturefood

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import androidx.camera.view.PreviewView
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityCaptureFoodBinding
import com.bruhascended.fitapp.util.setupToolbar


class CaptureFoodActivity : CameraActivity() {

    private lateinit var binding: ActivityCaptureFoodBinding
    private lateinit var predictionPresenter: PredictionPresenter


    override lateinit var imageAnalyzer: ImageStreamAnalyzer
    override lateinit var cameraViewFinder: PreviewView

    private fun setupFlashlightFab() {
        binding.flashlightFab.apply {
            setOnClickListener {
                setImageResource(
                    if (flashlightState)
                        R.drawable.anim_flashlight_to_on
                    else
                        R.drawable.anim_flashlight_to_off
                )
                val animatedDrawable = drawable as AnimatedVectorDrawable
                toggleFlashlight()
                animatedDrawable.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture_food)
        setupToolbar(binding.toolbar, home = true)

        cameraViewFinder = binding.viewFinder
        predictionPresenter = PredictionPresenter(this, binding.predictionRecyclerView)

        imageAnalyzer =  ImageStreamAnalyzer (this) {
            predictionPresenter.populate(it)
        }

        requestCameraPermissionsAndStart()

        setupFlashlightFab()
    }
}
