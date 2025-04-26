package com.senlin.budgetmaster.ui.goal.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    val goalListUiState: StateFlow<GoalListUiState> =
        budgetRepository.getAllGoals() // Correct method name
            .map { GoalListUiState(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = GoalListUiState()
            )

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            budgetRepository.deleteGoal(goal)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class GoalListUiState(val goalList: List<Goal> = listOf())
