package com.bruhascended.fitapp.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.settings.fragments.MainFragment
import com.bruhascended.fitapp.util.setupToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, MainFragment())
                .commit()
        }
        setupToolbar(findViewById(R.id.toolbar_settings))
    }
}