package com.senlin.budgetmaster.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Represents the UI state for the Transaction List screen.
 */
data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    // Add other state properties like error messages if needed
)

class TransactionListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<TransactionListUiState> =
        budgetRepository.getAllTransactions()
            .map { transactions -> TransactionListUiState(transactions = transactions) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = TransactionListUiState(isLoading = true) // Initial state shows loading
            )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L // 5 seconds
    }

    // TODO: Add functions for deleting or interacting with transactions if needed
    // Example:
    // fun deleteTransaction(transaction: Transaction) {
    //     viewModelScope.launch {
    //         budgetRepository.deleteTransaction(transaction)
    //     }
    // }
}
