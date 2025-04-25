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

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            val transactionsFlow = budgetRepository.getAllTransactions()
            val goalsFlow = budgetRepository.getAllGoals()

            combine(transactionsFlow, goalsFlow) { transactions, goals ->
                val balance = transactions.sumOf {
                    if (it.type == TransactionType.INCOME) it.amount else -it.amount
                }
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
