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
    val isLoading: Boolean = true, // Start as loading until userId is confirmed
    val categoryMap: Map<Long, String> = emptyMap(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val currentUserId: Long? = null // Track the user ID
    // Add other state properties like error messages if needed
)

@OptIn(ExperimentalCoroutinesApi::class) // Needed for flatMapLatest
class TransactionListViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    // StateFlows for selected dates
    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()

    // StateFlow for the current user ID - to be set by the UI layer
    private val _currentUserId = MutableStateFlow<Long?>(null)

    // Flow for categories, dependent on userId
    private val categoriesFlow: Flow<List<Category>> = _currentUserId.flatMapLatest { userId ->
        if (userId != null && userId != 0L) {
            budgetRepository.getAllCategories(userId)
        } else {
            flowOf(emptyList()) // No user, no categories
        }
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), replay = 1)

    // Combine userId, date filters, and categories to fetch transactions
    val uiState: StateFlow<TransactionListUiState> =
        combine(_currentUserId, _startDate, _endDate) { userId, start, end ->
            Triple(userId, start, end) // Combine userId and dates
        }.flatMapLatest { (userId, start, end) ->
            if (userId == null || userId == 0L) {
                // If no valid user ID, return a default/loading state immediately
                flowOf(TransactionListUiState(isLoading = true, currentUserId = null))
            } else {
                // Fetch transactions based on the userId and date range
                val transactionsFlow = if (start != null && end != null) {
                    if (!start.isAfter(end)) {
                        budgetRepository.getTransactionsBetweenDates(userId, start, end)
                    } else {
                        flowOf(emptyList())
                    }
                } else {
                    budgetRepository.getAllTransactions(userId) // Fetch all for the user
                }

                // Combine the determined transactions flow with the categories flow
                combine(transactionsFlow, categoriesFlow) { transactions, categories ->
                    val categoryMap = categories.associateBy({ it.id }, { it.name })
                    TransactionListUiState(
                        transactions = transactions,
                        categoryMap = categoryMap,
                        startDate = start,
                        endDate = end,
                        currentUserId = userId, // Include userId in state
                        isLoading = false // Data loaded for this user
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = TransactionListUiState(isLoading = true) // Initial state: loading
        )

    // --- Public functions ---

    fun setCurrentUserId(userId: Long?) {
        _currentUserId.value = userId
    }

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

    // TODO: Update deleteTransaction if added, to ensure it uses the correct userId implicitly via the transaction object
    // fun deleteTransaction(transaction: Transaction) {
    //     viewModelScope.launch {
    //         // Ensure transaction object has the correct userId before deleting
    //         if (transaction.userId == _currentUserId.value) {
    //              budgetRepository.deleteTransaction(transaction)
    //         } else {
    //              // Handle error or log: mismatch user ID
    //         }
    //     }
    // }
}
