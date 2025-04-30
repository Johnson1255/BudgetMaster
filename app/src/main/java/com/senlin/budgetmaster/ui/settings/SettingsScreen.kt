package com.senlin.budgetmaster.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R
import com.senlin.budgetmaster.ui.ViewModelFactory // Assuming ViewModelFactory is accessible

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = ViewModelFactory.Factory) // Use ViewModelFactory
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.settings_title), // Need to add this string
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LanguageSelection(
            selectedLanguageCode = uiState.selectedLanguageCode,
            onLanguageSelected = { viewModel.updateLanguage(it) }
        )

        // Add other settings sections here if needed in the future
    }
}

@Composable
fun LanguageSelection(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        "en" to stringResource(id = R.string.language_english), // Need to add this string
        "es" to stringResource(id = R.string.language_spanish)  // Need to add this string
    )

    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.language_setting_title), // Need to add this string
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        languages.forEach { (code, name) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (code == selectedLanguageCode),
                        onClick = { onLanguageSelected(code) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (code == selectedLanguageCode),
                    onClick = null // Recommended for accessibility with selectable Row
                )
                Spacer(Modifier.width(8.dp))
                Text(text = name)
            }
        }
    }
}
