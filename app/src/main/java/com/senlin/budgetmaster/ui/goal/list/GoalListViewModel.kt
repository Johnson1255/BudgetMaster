package com.senlin.budgetmaster.ui.goal.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class GoalListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    // StateFlow for the current user ID - to be set by the UI layer
    private val _currentUserId = MutableStateFlow<Long?>(null)

    val goalListUiState: StateFlow<GoalListUiState> = _currentUserId.flatMapLatest { userId ->
        if (userId == null || userId == 0L) {
            // No valid user, return loading/empty state
            flowOf(GoalListUiState(isLoading = true, currentUserId = null))
        } else {
            // Valid user, fetch goals
            budgetRepository.getAllGoals(userId)
                .map { goals ->
                    GoalListUiState(
                        goalList = goals,
                        isLoading = false,
                        currentUserId = userId
                    )
                }
                .catch { exception ->
                    // Emit error state
                    emit(GoalListUiState(
                        isLoading = false,
                        error = "Failed to load goals: ${exception.message}",
                        currentUserId = userId
                    ))
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = GoalListUiState(isLoading = true) // Initial state is loading
    )

    fun setCurrentUserId(userId: Long?) {
        _currentUserId.value = userId
    }

    fun deleteGoal(goal: Goal) {
        // Ensure the goal belongs to the current user before deleting
        if (goal.userId == _currentUserId.value) {
            viewModelScope.launch {
                try {
                    budgetRepository.deleteGoal(goal)
                    // StateFlow should automatically update
                } catch (e: Exception) {
                    // Handle deletion error if needed, e.g., update UI state
                    println("Error deleting goal: ${e.message}")
                }
            }
        } else {
            println("Error: Attempted to delete goal belonging to another user.")
            // Optionally set an error message in the UI state
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GoalListUiState(
    val goalList: List<Goal> = emptyList(),
    val isLoading: Boolean = true, // Add loading state
    val error: String? = null,     // Add error state
    val currentUserId: Long? = null // Add user ID state
)
