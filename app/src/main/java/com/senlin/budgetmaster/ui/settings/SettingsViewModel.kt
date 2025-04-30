package com.senlin.budgetmaster.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedLanguageCode: String = "en" // Default to English
)

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userSettingsRepository.languagePreference
        .map { languageCode ->
            SettingsUiState(selectedLanguageCode = languageCode ?: "en") // Use "en" if null
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            userSettingsRepository.saveLanguagePreference(languageCode)
        }
    }
}
