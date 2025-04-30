package com.senlin.budgetmaster.ui

import androidx.lifecycle.ViewModel // Already present, but good to confirm
import androidx.lifecycle.viewModelScope // Already present, but good to confirm
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository // Already present, but good to confirm
import kotlinx.coroutines.flow.SharingStarted // Already present, but good to confirm
import kotlinx.coroutines.flow.StateFlow // Add StateFlow import
import kotlinx.coroutines.flow.map // Add map import
import kotlinx.coroutines.flow.stateIn // Add stateIn import

// Simple state holder for the Main Activity/App composable
data class MainUiState(
    val initialLanguageSet: Boolean? = null // null means loading, true/false otherwise
)

class MainViewModel(userSettingsRepository: UserSettingsRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = userSettingsRepository.languagePreference
        .map { languageCode ->
            MainUiState(initialLanguageSet = languageCode != null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000), // Keep state for 5s after last subscriber
            initialValue = MainUiState() // Initial state with null (loading)
        )
}
