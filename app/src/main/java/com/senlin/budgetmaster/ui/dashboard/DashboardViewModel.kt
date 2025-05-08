package com.senlin.budgetmaster.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class DashboardUiState(
    val balance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DashboardViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Removed init block, data loading should be triggered by the UI when userId is available

    fun loadDataForUser(userId: Long) {
        // Prevent loading if userId is invalid (though UI should ideally handle this)
        if (userId == 0L) {
            _uiState.value = DashboardUiState(isLoading = false, errorMessage = "Invalid user ID.")
            return
        }
        _uiState.value = DashboardUiState(isLoading = true) // Set loading state

        viewModelScope.launch {
            val transactionsFlow = budgetRepository.getAllTransactions(userId)
            val goalsFlow = budgetRepository.getAllGoals(userId)

            combine(transactionsFlow, goalsFlow) { transactions, goals ->
                // Filter transactions to exclude those linked to a goal for balance calculation
                val nonGoalTransactions = transactions.filter { it.goalId == null }
                val balance = nonGoalTransactions.sumOf {
                    if (it.type == TransactionType.INCOME) it.amount else -it.amount
                }
                // Keep showing all recent transactions (or filter if preferred)
                val recentTransactions = transactions.sortedByDescending { it.date }.take(5)

                DashboardUiState(
                    balance = balance,
                    recentTransactions = recentTransactions,
                    goals = goals,
                    isLoading = false // Data loaded
                )
            }.catch { e ->
                // Handle error
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    errorMessage = "Failed to load dashboard data: ${e.localizedMessage}"
                )
            }.collect { newState ->
                // Update state
                _uiState.value = newState
            }
        }
    }
}
