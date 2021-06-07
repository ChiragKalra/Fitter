package com.bruhascended.fitapp.util

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

fun AppCompatActivity.setupToolbar(
    toolbar: Toolbar, mTitle: String? = null, home : Boolean = true
) {
    setSupportActionBar(toolbar)
    supportActionBar?.apply {
        setDisplayHomeAsUpEnabled(home)
        setDisplayShowHomeEnabled(home)
        title = mTitle ?: return
    }
}
