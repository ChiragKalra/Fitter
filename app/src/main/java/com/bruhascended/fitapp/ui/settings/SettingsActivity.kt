package com.bruhascended.fitapp.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.ui.settings.fragments.MainFragment
import com.bruhascended.fitapp.util.applyStatusBarPadding
import com.bruhascended.fitapp.util.setupToolbar

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Fitter_Settings)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar_settings)
        toolbar.applyStatusBarPadding(consumesTopStatusBarInset = true)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, MainFragment())
                .commit()
        }
        setupToolbar(toolbar)
    }
}