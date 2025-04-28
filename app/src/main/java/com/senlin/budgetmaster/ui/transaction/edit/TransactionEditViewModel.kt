package com.senlin.budgetmaster.ui.transaction.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Removed duplicate imports
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType // Import from model
import com.senlin.budgetmaster.navigation.Screen // Import Screen
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first // Import first extension
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate // Use java.time.LocalDate
// Removed unused imports for custom first()
import java.util.NoSuchElementException // Keep if needed elsewhere, but likely not for first()

data class TransactionEditUiState(
    val transactionId: Long? = null, // Use Long for ID
    val amount: String = "", // Keep String for TextField binding
    val type: TransactionType = TransactionType.EXPENSE, // Use model's enum
    val selectedCategory: Category? = null,
    val availableCategories: List<Category> = emptyList(),
    val date: LocalDate = LocalDate.now(), // Use java.time.LocalDate
    val note: String = "",
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val isLoading: Boolean = true // Start in loading state
)

// Remove local enum definition, use the one from the model
// enum class TransactionType {
//    INCOME, EXPENSE
// }

class TransactionEditViewModel(
    private val repository: BudgetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditUiState())
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    // Get transactionId as Long? from SavedStateHandle
    private val transactionId: Long? = savedStateHandle[Screen.TRANSACTION_ID_ARG] // Use key from Screen

    init {
        // Ensure the ID from navigation (-1) is treated as null for new transaction logic
        val idToLoad = if (transactionId == -1L) null else transactionId
        loadInitialData(idToLoad)
    }

    // Pass the potentially null ID
    private fun loadInitialData(idToLoad: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Use correct repository method names
                 val categoriesFlow = repository.getAllCategories() // Corrected method name
                 val categories = categoriesFlow.first() // Collect first emission

                if (idToLoad != null) { // Editing existing transaction
                     val transactionFlow = repository.getTransactionById(idToLoad) // Corrected method name
                     val transaction = transactionFlow.first()

                    if (transaction != null) {
                        _uiState.update {
                            it.copy(
                                transactionId = transaction.id, // Long
                                amount = transaction.amount.toString(), // Convert Double to String
                                type = transaction.type, // Use type from model
                                selectedCategory = categories.find { c -> c.id == transaction.categoryId }, // Match Long IDs
                                availableCategories = categories,
                                date = transaction.date, // java.time.LocalDate
                                note = transaction.note ?: "",
                                isLoading = false
                            )
                        }
                    } else {
                        // Handle case where transaction ID is invalid but was provided
                        _uiState.update { it.copy(isLoading = false, isError = true, availableCategories = categories) }
                    }
                } else {
                    // New transaction mode
                    _uiState.update {
                        it.copy(
                            availableCategories = categories,
                            isLoading = false,
                            // Select first category as default if available
                            selectedCategory = categories.firstOrNull() // Use firstOrNull for safety
                        )
                    }
                }
            } catch (e: Exception) {
                 // Log the exception e.printStackTrace() or use a proper logger
                _uiState.update { it.copy(isLoading = false, isError = true) }
            }
        }
    }

     fun updateAmount(newAmount: String) {
        // Basic validation can be added here if needed (e.g., regex for numbers)
        _uiState.update { it.copy(amount = newAmount) }
    }

    fun updateType(newType: TransactionType) { // Parameter uses model's enum
        _uiState.update { it.copy(type = newType) }
    }

    fun updateCategory(newCategory: Category) {
        _uiState.update { it.copy(selectedCategory = newCategory) }
    }

    fun updateDate(newDate: LocalDate) { // Parameter is java.time.LocalDate
        _uiState.update { it.copy(date = newDate) }
    }

    fun updateNote(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun saveTransaction() {
        val currentState = uiState.value
        if (currentState.isSaving || currentState.selectedCategory == null) return // Prevent double saves or saving without category

        val amountDouble = try {
            // Convert amount string to Double
            currentState.amount.toDouble()
        } catch (e: NumberFormatException) {
            _uiState.update { it.copy(isError = true) } // Show error if amount is invalid
            return
        }

        _uiState.update { it.copy(isSaving = true, isError = false) }

        viewModelScope.launch {
            try {
                val transactionToSave = Transaction(
                    // Use 0L for new transaction ID, Room will auto-generate
                    id = currentState.transactionId ?: 0L,
                    amount = amountDouble, // Use Double
                    type = currentState.type, // Use TransactionType enum
                    // Use categoryId (Long), non-null asserted due to check above
                    categoryId = currentState.selectedCategory!!.id,
                    date = currentState.date, // Use LocalDate
                    note = currentState.note.takeIf { it.isNotBlank() } // Store null if note is blank
                )

                if (transactionToSave.id == 0L) {
                    // Assuming repository method exists
                    repository.insertTransaction(transactionToSave)
                } else {
                    // Assuming repository method exists
                    repository.updateTransaction(transactionToSave)
                }
                // State update to signal save completion can be added if navigation depends on it
                 _uiState.update { it.copy(isSaving = false) } // Reset saving state after success/failure
            } catch (e: Exception) {
                 // Log the exception e.printStackTrace() or use a proper logger
                _uiState.update { it.copy(isSaving = false, isError = true) }
            }
        }
    }
}

// Removed custom first() extension function. Using kotlinx.coroutines.flow.first() instead.
