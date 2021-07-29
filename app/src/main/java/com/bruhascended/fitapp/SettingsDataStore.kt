package com.bruhascended.fitapp

import android.content.Context
import androidx.preference.PreferenceDataStore
import com.bruhascended.fitapp.repository.UserPreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsDataStore(val context: Context) : PreferenceDataStore() {
    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return runBlocking {
            UserPreferenceRepository(context).getPreference(UserPreferenceRepository.PreferenceKeys.SYNC_ENABLED)
                .first() ?: false
        }
    }

    override fun putBoolean(key: String?, value: Boolean) {
        CoroutineScope(IO).launch {
            UserPreferenceRepository(context).updateSyncEnabled(value)
        }
    }
}