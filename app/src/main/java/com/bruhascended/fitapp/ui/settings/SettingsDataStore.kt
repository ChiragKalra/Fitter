package com.bruhascended.fitapp.ui.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import com.bruhascended.fitapp.repository.PreferencesKeys
import com.bruhascended.fitapp.repository.PreferencesRepository

class SettingsDataStore(context: Context) : PreferenceDataStore() {
    private val repo = PreferencesRepository(context)

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return if (key != null) {
            repo.getPreference(PreferencesKeys.BoolPreferences.valueOf(key).key)?.toString()
                ?.toBooleanStrictOrNull()
                ?: false
        } else defValue
    }

    override fun putBoolean(key: String?, value: Boolean) {
        if (key != null) {
            repo.updatePreference(PreferencesKeys.BoolPreferences.valueOf(key).key, value)
        }
    }

//    override fun getString(key: String?, defValue: String?): String? {
//        return if (key != null) {
//            repo.getPreference(PreferencesKeys.IntPreferences.valueOf(key).key).toString()
//        } else defValue
//    }

    override fun putString(key: String?, value: String?) {
        if (key != null && !value.isNullOrEmpty()) {
            repo.updatePreference(PreferencesKeys.LongPreferences.valueOf(key).key, value.toLong())
        }
    }
}