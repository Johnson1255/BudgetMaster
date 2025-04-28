package com.senlin.budgetmaster.ui.transaction.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate

data class TransactionEditUiState(
    val transactionId: Int? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE, // Default to Expense
    val selectedCategory: Category? = null,
    val availableCategories: List<Category> = emptyList(),
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val isLoading: Boolean = true // Start in loading state
)

enum class TransactionType {
    INCOME, EXPENSE
}

class TransactionEditViewModel(
    private val repository: BudgetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditUiState())
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    private val transactionId: Int? = savedStateHandle["transactionId"]

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val categories = repository.getAllCategoriesStream().first() // Get initial list
                if (transactionId != null && transactionId != -1) { // Check for valid ID for editing
                    val transaction = repository.getTransactionStream(transactionId).first()
                    if (transaction != null) {
                        _uiState.update {
                            it.copy(
                                transactionId = transaction.id,
                                amount = transaction.amount.toPlainString(),
                                type = if (transaction.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                selectedCategory = categories.find { c -> c.id == transaction.categoryId },
                                availableCategories = categories,
                                date = transaction.date,
                                note = transaction.note ?: "",
                                isLoading = false
                            )
                        }
                    } else {
                        // Handle case where transaction ID is invalid
                        _uiState.update { it.copy(isLoading = false, isError = true, availableCategories = categories) }
                    }
                } else {
                    // New transaction mode
                    _uiState.update {
                        it.copy(
                            availableCategories = categories,
                            isLoading = false,
                            // Select first category as default if available
                            selectedCategory = categories.firstOrNull()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

     fun updateAmount(newAmount: String) {
        // Basic validation can be added here if needed
        _uiState.update { it.copy(amount = newAmount) }
    }

    fun updateType(newType: TransactionType) {
        _uiState.update { it.copy(type = newType) }
    }

    fun updateCategory(newCategory: Category) {
        _uiState.update { it.copy(selectedCategory = newCategory) }
    }

    fun updateDate(newDate: LocalDate) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun updateNote(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun saveTransaction() {
        if (_uiState.value.isSaving || _uiState.value.selectedCategory == null) return // Prevent double saves or saving without category

        val amountDecimal = try {
            BigDecimal(_uiState.value.amount)
        } catch (e: NumberFormatException) {
            _uiState.update { it.copy(isError = true) } // Show error if amount is invalid
            return
        }

        _uiState.update { it.copy(isSaving = true, isError = false) }

        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = _uiState.value.transactionId ?: 0, // 0 for Room to auto-generate if new
                    amount = amountDecimal,
                    isIncome = _uiState.value.type == TransactionType.INCOME,
                    categoryId = _uiState.value.selectedCategory!!.id, // Non-null asserted due to check above
                    date = _uiState.value.date,
                    note = _uiState.value.note.takeIf { it.isNotBlank() } // Store null if note is blank
                )

                if (transaction.id == 0) {
                    repository.insertTransaction(transaction)
                } else {
                    repository.updateTransaction(transaction)
                }
                // State update to signal save completion can be added if navigation depends on it
                 _uiState.update { it.copy(isSaving = false) } // Reset saving state
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, isError = true) }
            }
        }
    }
}
// Helper extension needed for collecting Flow once
suspend fun <T> kotlinx.coroutines.flow.Flow<T>.first(): T {
    var result: T? = null
    kotlinx.coroutines.flow.collect { value ->
        result = value
        throw kotlinx.coroutines.CancellationException() // Stop collecting after the first item
    }
    return result ?: throw NoSuchElementException("Flow was empty")
}
