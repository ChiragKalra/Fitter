package com.bruhascended.fitapp.ui.logweight

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.bruhascended.db.weight.entities.WeightEntry
import com.bruhascended.db.weight.types.WeightType
import com.bruhascended.fitapp.R
import com.bruhascended.fitapp.reminders.WeightReminderScheduler
import com.bruhascended.fitapp.repository.WeightEntryRepository
import com.bruhascended.fitapp.ui.theme.FitAppTheme
import kotlinx.coroutines.launch

class LogWeightActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = WeightEntryRepository(this)
        setContent {
            FitAppTheme {
                LogWeightScreen(
                    onBack = { finish() },
                    onSave = { weight ->
                        lifecycleScope.launch {
                            repo.writeEntry(
                                WeightEntry(
                                    weight = weight,
                                    type = WeightType.Kilogram,
                                    timeInMillis = System.currentTimeMillis(),
                                )
                            )
                            WeightReminderScheduler.rescheduleAll(this@LogWeightActivity)
                            Toast.makeText(
                                this@LogWeightActivity,
                                getString(R.string.weight_log_saved),
                                Toast.LENGTH_SHORT,
                            ).show()
                            finish()
                        }
                    },
                )
            }
        }
    }

    @Composable
    private fun LogWeightScreen(
        onBack: () -> Unit,
        onSave: (Double) -> Unit,
    ) {
        var rawWeight by remember { mutableStateOf("") }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TopAppBar(
                title = { Text(getString(R.string.log_weight)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = getString(R.string.back))
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface,
                elevation = 0.dp,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                OutlinedTextField(
                    value = rawWeight,
                    onValueChange = { rawWeight = it },
                    label = { Text(getString(R.string.weight_log_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val weight = rawWeight.toDoubleOrNull()
                        if (weight == null || weight <= 0.0) {
                            Toast.makeText(
                                this@LogWeightActivity,
                                getString(R.string.weight_log_invalid),
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            onSave(weight)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(getString(R.string.submit))
                }
            }
        }
    }
}
