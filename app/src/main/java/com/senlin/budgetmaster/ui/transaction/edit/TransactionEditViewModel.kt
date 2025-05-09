package com.senlin.budgetmaster.ui.transaction.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.navigation.Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import java.time.LocalDate

// Define a constant for the placeholder category name
private const val GOAL_CONTRIBUTION_CATEGORY_NAME = "Goal Contribution"

data class TransactionEditUiState(
    val transactionId: Long? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedItem: Any? = null, // Can be Category or Goal
    val availableItems: List<Any> = emptyList(), // Combined list
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val goalContributionCategoryId: Long? = null,
    val currentUserId: Long? = null // Added userId
)

class TransactionEditViewModel(
    private val repository: BudgetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditUiState())
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    private val _saveSuccessEvent = MutableSharedFlow<Unit>(replay = 0)
    val saveSuccessEvent = _saveSuccessEvent.asSharedFlow()

    private val transactionId: Long? = savedStateHandle[Screen.TRANSACTION_ID_ARG]

    // Property to hold the current user ID
    private var currentUserId: Long? = null

    // Call this from the UI layer once the userId is available
    fun initialize(userId: Long) {
        if (currentUserId == null) { // Prevent re-initialization
            currentUserId = userId
            _uiState.update { it.copy(currentUserId = userId) }
            val idToLoad = if (transactionId == -1L) null else transactionId
            loadInitialData(userId, idToLoad)
        }
    }


    private fun loadInitialData(userId: Long, idToLoad: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch categories and goals for the specific user
                val categoriesDeferred = viewModelScope.async { repository.getAllCategories(userId).first() }
                val goalsDeferred = viewModelScope.async { repository.getAllGoals(userId).first() }

                var categories = categoriesDeferred.await()
                val goals = goalsDeferred.await()

                // Find or create the "Goal Contribution" category for this user
                var goalContributionCategory = categories.find { it.name == GOAL_CONTRIBUTION_CATEGORY_NAME }
                var goalContributionCategoryId = goalContributionCategory?.id

                if (goalContributionCategory == null) {
                    val newCategory = Category(name = GOAL_CONTRIBUTION_CATEGORY_NAME, userId = userId) // Set userId
                    repository.insertCategory(newCategory)
                    // Re-fetch categories to get the ID
                    categories = repository.getAllCategories(userId).first() // Fetch again for this user
                    goalContributionCategory = categories.find { it.name == GOAL_CONTRIBUTION_CATEGORY_NAME }
                    goalContributionCategoryId = goalContributionCategory?.id
                }

                val combinedItems: List<Any> = categories.filterNot { it.id == goalContributionCategoryId } + goals

                var initialSelectedItem: Any? = null
                if (idToLoad != null) {
                    val transaction = repository.getTransactionById(idToLoad, userId).first() // Use userId
                    if (transaction != null) {
                        initialSelectedItem = if (transaction.goalId != null) {
                            goals.find { it.id == transaction.goalId }
                        } else {
                            categories.find { it.id == transaction.categoryId }
                        }

                        _uiState.update { state ->
                            state.copy(
                                transactionId = transaction.id,
                                amount = transaction.amount.toString(),
                                type = transaction.type,
                                selectedItem = initialSelectedItem,
                                availableItems = combinedItems,
                                date = transaction.date,
                                note = transaction.note ?: "",
                                isLoading = false,
                                goalContributionCategoryId = goalContributionCategoryId
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Transaction not found.", availableItems = combinedItems, goalContributionCategoryId = goalContributionCategoryId) }
                    }
                } else {
                    // New transaction
                    _uiState.update { state ->
                        state.copy(
                            availableItems = combinedItems,
                            isLoading = false,
                            selectedItem = combinedItems.firstOrNull(),
                            goalContributionCategoryId = goalContributionCategoryId
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to load data: ${e.message}") }
            }
        }
    }

    fun updateAmount(newAmount: String) {
        _uiState.update { it.copy(amount = newAmount, errorMessage = null) } // Clear error on update
    }

    fun updateType(newType: TransactionType) {
        _uiState.update { it.copy(type = newType) }
    }

    fun updateSelectedItem(item: Any?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    fun updateDate(newDate: LocalDate) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun updateNote(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    fun saveTransaction() {
        val userId = currentUserId // Get the stored userId
        if (userId == null || userId == 0L) {
            _uiState.update { it.copy(errorMessage = "User not identified.", isSaving = false) } // Reset isSaving
            return
        }

        val currentState = uiState.value
        if (currentState.isSaving || currentState.selectedItem == null ||
            (currentState.selectedItem is Goal && currentState.goalContributionCategoryId == null)) {
            _uiState.update { it.copy(errorMessage = "Please select a category or goal.") }
            return
        }

        val amountDouble = currentState.amount.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid positive amount.") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val selectedItem = currentState.selectedItem

                // Validation for Goal Overspending
                if (selectedItem is Goal && currentState.type == TransactionType.EXPENSE) {
                    // Fetch goal using userId
                    val goal = repository.getGoalById(selectedItem.id, userId).firstOrNull()
                    if (goal != null && amountDouble > goal.currentAmount) {
                        _uiState.update { state ->
                            state.copy(
                                isSaving = false,
                                errorMessage = "Insufficient funds in the selected goal."
                            )
                        }
                        return@launch
                    }
                }

                val categoryIdToSave: Long
                val goalIdToSave: Long?

                when (selectedItem) {
                    is Category -> {
                        categoryIdToSave = selectedItem.id
                        goalIdToSave = null
                    }
                    is Goal -> {
                        categoryIdToSave = currentState.goalContributionCategoryId!! // Already checked not null
                        goalIdToSave = selectedItem.id
                    }
                    else -> {
                        _uiState.update { it.copy(isSaving = false, errorMessage = "Invalid item selected.") }
                        return@launch
                    }
                }

                val transactionToSave = Transaction(
                    id = currentState.transactionId ?: 0L,
                    userId = userId, // Set the userId
                    amount = amountDouble,
                    type = currentState.type,
                    categoryId = categoryIdToSave,
                    goalId = goalIdToSave,
                    date = currentState.date,
                    note = currentState.note.takeIf { it.isNotBlank() }
                )

                if (transactionToSave.id == 0L) {
                    repository.insertTransaction(transactionToSave)
                } else {
                    repository.updateTransaction(transactionToSave)
                }
                _uiState.update { it.copy(isSaving = false) }
                _saveSuccessEvent.emit(Unit)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save transaction: ${e.message}") }
            }
        }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
