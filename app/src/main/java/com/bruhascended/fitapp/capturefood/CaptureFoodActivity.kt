package com.bruhascended.fitapp.capturefood

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
    }
}
