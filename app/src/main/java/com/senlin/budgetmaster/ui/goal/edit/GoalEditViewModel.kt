package com.senlin.budgetmaster.ui.goal.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class GoalEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"])

    var goalUiState by mutableStateOf(GoalUiState())
        private set

    init {
        viewModelScope.launch {
            if (goalId > 0) {
                goalUiState = budgetRepository.getGoalById(goalId) // Correct method name
                    .filterNotNull()
                    .first()
                    .toGoalUiState(isEntryValid = true) // Assume valid initially when loading existing
            }
        }
    }

    fun updateUiState(newGoalUiState: GoalUiState) {
        goalUiState = newGoalUiState.copy(isEntryValid = validateInput(newGoalUiState))
    }

    suspend fun saveGoal() {
        if (validateInput()) {
            val goalToSave = goalUiState.toGoal()
            if (goalId > 0) {
                budgetRepository.updateGoal(goalToSave)
            } else {
                budgetRepository.insertGoal(goalToSave)
            }
        }
    }

    private fun validateInput(uiState: GoalUiState = goalUiState): Boolean {
        return with(uiState) {
            name.isNotBlank() && targetAmount.isNotBlank() && targetAmount.toDoubleOrNull() != null && targetAmount.toDouble() > 0
            // Add currentAmount validation if needed, though it often starts at 0
            // Add targetDate validation if needed
        }
    }
}

data class GoalUiState(
    val id: Long = 0,
    val name: String = "",
    val targetAmount: String = "",
    val currentAmount: String = "0.0", // Keep as String for TextField
    val targetDate: Date? = null,
    val creationDate: Date = Date(), // Set on creation/load
    val isEntryValid: Boolean = false
)

fun Goal.toGoalUiState(isEntryValid: Boolean = false): GoalUiState = GoalUiState(
    id = id,
    name = name,
    targetAmount = targetAmount.toString(),
    currentAmount = currentAmount.toString(),
    targetDate = targetDate,
    creationDate = creationDate,
    isEntryValid = isEntryValid
)

fun GoalUiState.toGoal(): Goal = Goal(
    id = id,
    name = name,
    targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
    currentAmount = currentAmount.toDoubleOrNull() ?: 0.0,
    targetDate = targetDate,
    creationDate = creationDate
)
