package com.senlin.budgetmaster.ui

import androidx.lifecycle.ViewModel // Already present, but good to confirm
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine // Import combine

// State holder for the Main Activity/App composable
data class MainUiState(
    val initialLanguageSet: Boolean? = null, // null means loading, true/false otherwise
    val selectedLanguageCode: String? = null, // Holds the current language code, null if loading
    val currentUserId: Long? = null, // Holds the current logged-in user ID, null if no user logged in
    val isLoading: Boolean = true // Combined loading state
)

class MainViewModel(userSettingsRepository: UserSettingsRepository) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        userSettingsRepository.languagePreference,
        userSettingsRepository.currentUserId
    ) { languageCode, userId ->
        MainUiState(
            initialLanguageSet = languageCode != null, // Consider this true once first value arrives
            selectedLanguageCode = languageCode,
            currentUserId = userId,
            isLoading = false // Set to false once both flows have emitted at least once
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState() // Initial state indicates loading
    )
}
