package com.bruhascended.fitapp.capturefood

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityCaptureFoodBinding
import com.bruhascended.fitapp.util.setupToolbar

class CaptureFoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptureFoodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_capture_food)
        setupToolbar(binding.toolbar, mTitle = "", home = true)

    }
}
