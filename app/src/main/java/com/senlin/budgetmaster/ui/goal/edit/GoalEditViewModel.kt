package com.senlin.budgetmaster.ui.goal.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.navigation.Screen // Import Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate // Use LocalDate

class GoalEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>(Screen.GOAL_ID_ARG) ?: 0L // Default to 0L for new goal

    var goalUiState by mutableStateOf(GoalUiState())
        private set

    private var currentUserId: Long? = null // Store userId

    // Call this from UI layer when userId is available
    fun initialize(userId: Long) {
        if (currentUserId == null) { // Prevent re-initialization
            currentUserId = userId
            // Initialize UI state with the userId, setting loading based on whether we need to fetch
            goalUiState = GoalUiState(currentUserId = userId, isLoading = (goalId != 0L))
            if (goalId != 0L) {
                loadGoal(userId, goalId)
            }
        }
    }

     private fun loadGoal(userId: Long, goalIdToLoad: Long) {
         viewModelScope.launch {
             // Ensure isLoading is true before starting fetch
             if (!goalUiState.isLoading) {
                 goalUiState = goalUiState.copy(isLoading = true)
             }
             try {
                 goalUiState = budgetRepository.getGoalById(goalIdToLoad, userId)
                     .filterNotNull()
                     .first()
                     .toGoalUiState(userId, isEntryValid = true, isLoading = false) // Pass userId
             } catch (e: Exception) {
                 goalUiState = goalUiState.copy(isLoading = false, error = "Failed to load goal: ${e.message}")
             }
         }
     }

    fun updateUiState(newGoalUiState: GoalUiState) {
        // Preserve the currentUserId when updating other fields
        goalUiState = newGoalUiState.copy(
            isEntryValid = validateInput(newGoalUiState),
            currentUserId = currentUserId // Ensure userId is maintained
        )
    }

    suspend fun saveGoal(): Boolean {
        val userId = currentUserId
        if (userId == null || userId == 0L) {
            goalUiState = goalUiState.copy(isLoading = false, error = "User not identified.")
            return false
        }
        if (!validateInput()) {
             goalUiState = goalUiState.copy(error = "Invalid input. Please check fields.") // Provide error message
            return false
        }
        goalUiState = goalUiState.copy(isLoading = true, error = null)
        return try {
            val goalToSave = goalUiState.toGoal(userId) // Pass userId
            if (goalToSave.id == 0L) { // Check against 0L for new goal
                budgetRepository.insertGoal(goalToSave)
            } else {
                budgetRepository.updateGoal(goalToSave)
            }
            goalUiState = goalUiState.copy(isLoading = false) // Indicate saving finished
            true // Indicate success
        } catch (e: Exception) {
            goalUiState = goalUiState.copy(isLoading = false, error = "Failed to save goal: ${e.message}")
            false // Indicate failure
        }
    }

    private fun validateInput(uiState: GoalUiState = goalUiState): Boolean {
        return with(uiState) {
            val targetAmountDouble = targetAmount.toDoubleOrNull()
            name.isNotBlank() && targetAmount.isNotBlank() && targetAmountDouble != null && targetAmountDouble > 0
        }
    }
}

data class GoalUiState(
    val id: Long = 0L, // Use 0L as default for new goal
    val name: String = "",
    val targetAmount: String = "",
    val currentAmount: String = "0.0",
    val targetDate: LocalDate? = null,
    val creationDate: LocalDate = LocalDate.now(),
    val isEntryValid: Boolean = false,
    val isLoading: Boolean = false, // Add loading state
    val error: String? = null,     // Add error state
    val currentUserId: Long? = null // Add user ID state
)

// Update extension function to include userId
fun Goal.toGoalUiState(userId: Long?, isEntryValid: Boolean = false, isLoading: Boolean = false): GoalUiState = GoalUiState(
    id = id,
    name = name,
    targetAmount = targetAmount.toString(),
    currentAmount = currentAmount.toString(),
    targetDate = targetDate,
    creationDate = creationDate,
    isEntryValid = isEntryValid,
    isLoading = isLoading,
    currentUserId = userId // Set userId
)

// Update extension function to include userId
fun GoalUiState.toGoal(userId: Long): Goal = Goal(
    id = id,
    userId = userId, // Set userId
    name = name,
    targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
    currentAmount = currentAmount.toDoubleOrNull() ?: 0.0,
    targetDate = targetDate,
    creationDate = creationDate // creationDate is already set in UiState
)
