package com.bruhascended.fitapp.ui.logweight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.databinding.ActivityLogWeightBinding
import com.bruhascended.fitapp.ui.util.setupToolbar

class LogWeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_log_weight)
        setupToolbar(binding.toolbar, home = true)
    }
}
