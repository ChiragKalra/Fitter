package com.bruhascended.fitapp.ui.capturefood

import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.TorchState
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityCaptureFoodBinding
import com.bruhascended.fitapp.util.applyStatusBarMarginTop
import com.bruhascended.fitapp.util.setupToolbar
import java.io.File


class CaptureFoodActivity : CameraActivity() {
    companion object {
        private const val TAG = "CaptureFoodActivity"
    }

    private lateinit var binding: ActivityCaptureFoodBinding
    private lateinit var predictionPresenter: PredictionPresenter

    override lateinit var imageAnalyzer: ImageAnalyzer

    override lateinit var cameraViewFinder: PreviewView

    private fun setupFlashlightFab() {
        binding.flashlightFab.apply {
            liveFlashlightState.observe(this@CaptureFoodActivity) {
                setImageResource(
                    if (it == TorchState.ON)
                        R.drawable.anim_flashlight_to_off
                    else
                        R.drawable.anim_flashlight_to_on
                )
                val animatedDrawable = drawable as AnimatedVectorDrawable
                animatedDrawable.start()
            }
            setOnClickListener {
                toggleFlashlight()
            }
        }
    }

    override fun onCameraStarted() {
        setupFlashlightFab()
        binding.smartCaptureButton.setOnClickListener {
            takeSmartCapturePicture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture_food)
        binding.appBarLayout.applyStatusBarMarginTop()
        setupToolbar(binding.toolbar, getString(R.string.smart_capture), home = true)

        cameraViewFinder = binding.viewFinder
        predictionPresenter = PredictionPresenter(this, binding.predictionRecyclerView)

        imageAnalyzer =  ImageAnalyzer (this) {
            predictionPresenter.populate(it)
        }

        requestCameraPermissionsAndStart()
    }

    private fun takeSmartCapturePicture() {
        binding.smartCaptureButton.isEnabled = false
        binding.flashlightFab.isEnabled = false

        val photoFile = File(cacheDir, "smart_capture_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    toggleFlashlight(false)
                    startActivity(
                        Intent(this@CaptureFoodActivity, SmartCaptureAnalysisActivity::class.java)
                            .putExtra(SmartCaptureAnalysisActivity.EXTRA_IMAGE_PATH, photoFile.absolutePath)
                    )
                    overridePendingTransition(android.R.anim.fade_in, R.anim.smart_capture_activity_close)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Smart Capture image capture failed", exception)
                    photoFile.delete()
                    Toast.makeText(
                        this@CaptureFoodActivity,
                        getString(R.string.smart_capture_failed, exception.message ?: ""),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.smartCaptureButton.isEnabled = true
                    binding.flashlightFab.isEnabled = true
                }
            }
        )
    }
}
