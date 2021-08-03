package com.bruhascended.fitapp.ui.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import com.bruhascended.fitapp.repository.BoolPreferences
import com.bruhascended.fitapp.repository.PreferencesRepository

class SettingsDataStore(context: Context) : PreferenceDataStore() {
    private val repo = PreferencesRepository(context)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return if (key != null) {
             repo.getPreference(BoolPreferences.valueOf(key).key)?.toString()
                ?.toBooleanStrictOrNull()
                ?: false
        } else defValue
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if (key != null) {
            repo.updatePreference(BoolPreferences.valueOf(key).key, value)
        }
    }
}