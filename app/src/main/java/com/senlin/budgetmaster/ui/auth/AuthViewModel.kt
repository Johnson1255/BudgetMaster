package com.senlin.budgetmaster.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.User
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthState {
    IDLE, LOADING, SUCCESS, ERROR
}

data class AuthScreenUiState(
    val authState: AuthState = AuthState.IDLE,
    val errorMessage: String? = null,
    val currentUserId: Long? = null // To navigate after successful auth
)

class AuthViewModel(
    private val budgetRepository: BudgetRepository,
    private val userSettingsRepository: UserSettingsRepository,
    // PasswordHasher is an object, so no need to inject if using it directly
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthScreenUiState())
    val uiState: StateFlow<AuthScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userSettingsRepository.currentUserId.collect { userId ->
                // This could be used to redirect if user is already logged in,
                // but login/register screens are usually explicit actions.
                // For now, primarily for post-auth navigation.
                if (userId != null && _uiState.value.authState == AuthState.SUCCESS) {
                     _uiState.value = _uiState.value.copy(currentUserId = userId)
                }
            }
        }
    }

    fun registerUser(username: String, passwordRaw: String) {
        viewModelScope.launch {
            _uiState.value = AuthScreenUiState(authState = AuthState.LOADING)
            try {
                val existingUser = budgetRepository.getUserByUsername(username)
                if (existingUser != null) {
                    _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = "Username already exists.")
                    return@launch
                }
                if (passwordRaw.length < 6) { // Basic validation
                     _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = "Password must be at least 6 characters.")
                    return@launch
                }

                val hashedPassword = PasswordHasher.hashPassword(passwordRaw)
                val newUser = User(username = username, hashedPassword = hashedPassword)
                val newUserId = budgetRepository.insertUser(newUser)
                if (newUserId > 0) {
                    userSettingsRepository.saveCurrentUserId(newUserId)
                    _uiState.value = AuthScreenUiState(authState = AuthState.SUCCESS)
                } else {
                    _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = "Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = e.message ?: "An unknown error occurred.")
            }
        }
    }

    fun loginUser(username: String, passwordRaw: String) {
        viewModelScope.launch {
            _uiState.value = AuthScreenUiState(authState = AuthState.LOADING)
            try {
                val user = budgetRepository.getUserByUsername(username)
                if (user == null) {
                    _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = "Invalid username or password.")
                    return@launch
                }

                if (PasswordHasher.verifyPassword(passwordRaw, user.hashedPassword)) {
                    userSettingsRepository.saveCurrentUserId(user.userId)
                    _uiState.value = AuthScreenUiState(authState = AuthState.SUCCESS)
                } else {
                    _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = "Invalid username or password.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthScreenUiState(authState = AuthState.ERROR, errorMessage = e.message ?: "An unknown error occurred.")
            }
        }
    }
    
    fun resetAuthState() {
        _uiState.value = AuthScreenUiState(authState = AuthState.IDLE, errorMessage = null, currentUserId = null)
    }

    fun logoutUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authState = AuthState.LOADING)
            try {
                // Clear the stored user ID. This will trigger observers in MainViewModel
                // to update its own currentUserId to null, leading to navigation to Login.
                userSettingsRepository.clearCurrentUserId() // Use the correct method
                _uiState.value = AuthScreenUiState(authState = AuthState.IDLE) // Reset to idle, navigation handled by MainViewModel
            } catch (e: Exception) {
                // Handle potential errors during logout, though clearing preferences is usually safe
                _uiState.value = _uiState.value.copy(
                    authState = AuthState.ERROR,
                    errorMessage = "Logout failed: ${e.message}"
                )
            }
        }
    }
}
