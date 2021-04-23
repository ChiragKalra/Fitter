package com.bruhascended.fitapp.addworkout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityAddWorkoutBinding
import com.bruhascended.fitapp.util.setupToolbar

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_workout)
        setupToolbar(binding.toolbar, home = true)
    }
}