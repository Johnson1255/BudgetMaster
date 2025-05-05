package com.senlin.budgetmaster.ui.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category // Import Category
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi // For flatMapLatest
import kotlinx.coroutines.flow.* // Import combine
import java.time.LocalDate // Import LocalDate

/**
 * Represents the UI state for the Transaction List screen.
 */
data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val categoryMap: Map<Long, String> = emptyMap(), // Add map for category names
    val startDate: LocalDate? = null, // Add start date state
    val endDate: LocalDate? = null,   // Add end date state
    // Add other state properties like error messages if needed
)

@OptIn(ExperimentalCoroutinesApi::class) // Needed for flatMapLatest
class TransactionListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    // StateFlows for selected dates
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()

    // Flow for categories (to avoid fetching repeatedly)
    private val categoriesFlow = budgetRepository.getAllCategories()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), replay = 1)

    // Combine date filters and categories to fetch transactions
    val uiState: StateFlow<TransactionListUiState> =
        combine(_startDate, _endDate) { start, end ->
            Pair(start, end) // Combine dates into a pair
        }.flatMapLatest { (start, end) ->
            // Fetch transactions based on the date range
            val transactionsFlow = if (start != null && end != null) {
                // Ensure start is before or equal to end before querying
                if (!start.isAfter(end)) {
                    budgetRepository.getTransactionsBetweenDates(start, end) // Use existing repo method
                } else {
                    // Handle invalid date range (e.g., return empty flow or show error)
                    flowOf(emptyList()) // Return empty list for now
                }
            } else {
                budgetRepository.getAllTransactions() // Fetch all if no range selected
            }

            // Combine the determined transactions flow with the categories flow
            combine(transactionsFlow, categoriesFlow) { transactions, categories ->
                val categoryMap = categories.associateBy({ it.id }, { it.name })
                TransactionListUiState(
                    transactions = transactions,
                    categoryMap = categoryMap,
                    startDate = start, // Pass dates to UI state
                    endDate = end,
                    isLoading = false // Data loaded
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            // Initial state: loading, no dates selected
            initialValue = TransactionListUiState(isLoading = true)
        )


    // --- Public functions to update dates ---

    fun setStartDate(date: LocalDate?) {
        _startDate.value = date
    }

    fun setEndDate(date: LocalDate?) {
        _endDate.value = date
    }

    fun clearDateFilter() {
        _startDate.value = null
        _endDate.value = null
    }


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
