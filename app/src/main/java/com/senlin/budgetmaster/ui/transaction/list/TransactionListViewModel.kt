package com.senlin.budgetmaster.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category // Import Category
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.* // Import combine

/**
 * Represents the UI state for the Transaction List screen.
 */
data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val categoryMap: Map<Long, String> = emptyMap(), // Add map for category names
    // Add other state properties like error messages if needed
)

class TransactionListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    val uiState: StateFlow<TransactionListUiState> =
        combine(
            budgetRepository.getAllTransactions(),
            budgetRepository.getAllCategories() // Fetch categories as well
        ) { transactions, categories ->
            // Create a map from category ID to category name
            val categoryMap = categories.associateBy({ it.id }, { it.name })
            TransactionListUiState(
                transactions = transactions,
                categoryMap = categoryMap,
                isLoading = false // Data loaded
            )
        }
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
