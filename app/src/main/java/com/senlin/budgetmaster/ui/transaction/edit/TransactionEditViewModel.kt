package com.senlin.budgetmaster.ui.transaction.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.data.repository.BudgetRepository // Ensure only this one exists
import com.senlin.budgetmaster.navigation.Screen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull // Add import for firstOrNull
import java.time.LocalDate
// Removed unused imports

// Define a constant for the placeholder category name
private const val GOAL_CONTRIBUTION_CATEGORY_NAME = "Goal Contribution"

// Wrapper interface or common base class could be used, but Any works for now
data class TransactionEditUiState(
    val transactionId: Long? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedItem: Any? = null, // Can be Category or Goal
    val availableItems: List<Any> = emptyList(), // Combined list
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val isLoading: Boolean = true,
    val goalContributionCategoryId: Long? = null // To store the ID of the placeholder category
)

class TransactionEditViewModel(
    private val repository: BudgetRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditUiState())
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    // Get transactionId as Long? from SavedStateHandle
    // Get transactionId as Long? from SavedStateHandle
    private val transactionId: Long? = savedStateHandle[Screen.TRANSACTION_ID_ARG]

    init {
        val idToLoad = if (transactionId == -1L) null else transactionId
        loadInitialData(idToLoad)
    }

    private fun loadInitialData(idToLoad: Long?) {
        viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true, isError = false) }
            try {
                // Fetch categories and goals concurrently within the launch block
                val categoriesDeferred = viewModelScope.async { repository.getAllCategories().first() } // Call async on viewModelScope
                val goalsDeferred = viewModelScope.async { repository.getAllGoals().first() } // Call async on viewModelScope

                val categories = categoriesDeferred.await()
                 val goals = goalsDeferred.await()

                // Find or create the "Goal Contribution" category
                var goalContributionCategory = categories.find { category -> category.name == GOAL_CONTRIBUTION_CATEGORY_NAME } // Use explicit 'category'
                if (goalContributionCategory == null) {
                     // If it doesn't exist, create it (assuming repository has insert method)
                    // This might need more robust handling (e.g., check if creation succeeded)
                    val newCategory = Category(name = GOAL_CONTRIBUTION_CATEGORY_NAME)
                    repository.insertCategory(newCategory)
                    // Re-fetch categories to get the ID
                    val updatedCategories = repository.getAllCategories().first() // Fetch again
                    goalContributionCategory = updatedCategories.find { category -> category.name == GOAL_CONTRIBUTION_CATEGORY_NAME } // Use explicit 'category'
                    // Consider error handling if category still not found/created
                }
                val goalContributionCategoryId = goalContributionCategory?.id

                // Use updatedCategories if fetched, otherwise original categories
                // Ensure explicit lambda parameter 'category' is used in find
                val currentCategories = if (goalContributionCategory != null && categories.find { category -> category.id == goalContributionCategoryId } == null) {
                    repository.getAllCategories().first()
                } else {
                    categories
                }

                val combinedItems: List<Any> = currentCategories.filterNot { category -> category.id == goalContributionCategoryId } + goals // Use explicit 'category'

                var initialSelectedItem: Any? = null
                 if (idToLoad != null) {
                     val transaction = repository.getTransactionById(idToLoad).first()
                     if (transaction != null) {
                         // Determine if it was linked to a goal or a regular category
                         initialSelectedItem = if (transaction.goalId != null) {
                             goals.find { goal -> goal.id == transaction.goalId } // Use explicit 'goal'
                         } else {
                             // Use currentCategories list which includes the potentially newly added one
                             currentCategories.find { category -> category.id == transaction.categoryId } // Use explicit 'category'
                         }

                        _uiState.update { state -> // Use explicit 'state'
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
                        // Transaction ID provided but not found
                        _uiState.update { state -> state.copy(isLoading = false, isError = true, availableItems = combinedItems, goalContributionCategoryId = goalContributionCategoryId) } // Use explicit 'state'
                    }
                } else {
                    // New transaction
                    _uiState.update { state -> // Use explicit 'state'
                        state.copy(
                            availableItems = combinedItems,
                            isLoading = false,
                            selectedItem = combinedItems.firstOrNull(), // Default selection
                            goalContributionCategoryId = goalContributionCategoryId
                        )
                    }
                }
            } catch (e: Exception) {
                // Log exception properly
                _uiState.update { state -> state.copy(isLoading = false, isError = true) } // Use explicit 'state'
            }
        }
    }

     fun updateAmount(newAmount: String) {
        _uiState.update { state -> state.copy(amount = newAmount) } // Use explicit 'state'
    }

    fun updateType(newType: TransactionType) {
        _uiState.update { state -> state.copy(type = newType) } // Use explicit 'state'
    }

    // Renamed from updateCategory/updateGoal
    fun updateSelectedItem(item: Any?) {
        _uiState.update { state -> state.copy(selectedItem = item) } // Use explicit 'state'
    }

     fun updateDate(newDate: LocalDate) {
         _uiState.update { state -> state.copy(date = newDate) } // Use explicit 'state'
     }

     fun updateNote(newNote: String) {
         _uiState.update { state -> state.copy(note = newNote) } // Use explicit 'state'
     }

     fun saveTransaction() {
         val currentState = uiState.value // This is the correct declaration
         // Ensure an item is selected and we have the goal contribution category ID if needed
         if (currentState.isSaving || currentState.selectedItem == null ||
             (currentState.selectedItem is Goal && currentState.goalContributionCategoryId == null)) {
             // Potentially set an error state to inform the user
             _uiState.update { state -> state.copy(isError = true) } // Use explicit 'state'
             return
         }

         val amountDouble = currentState.amount.toDoubleOrNull()
         if (amountDouble == null || amountDouble <= 0) { // Also ensure amount is positive
             _uiState.update { state -> state.copy(isError = true, isSaving = false) } // Set error, stop saving
             return
         }

        _uiState.update { state -> state.copy(isSaving = true, isError = false) }

        viewModelScope.launch {
             try {
                val selectedItem = currentState.selectedItem // Shadow variable for smart casting

                // --- Validation for Goal Overspending ---
                if (selectedItem is Goal && currentState.type == TransactionType.EXPENSE) {
                    val goal = repository.getGoalById(selectedItem.id).firstOrNull()
                    if (goal != null && amountDouble > goal.currentAmount) {
                        // Trying to spend more than available in the goal
                        _uiState.update { state ->
                            state.copy(
                                isSaving = false,
                                isError = true // Consider adding a specific error message here
                            )
                        }
                        return@launch // Stop the saving process
                    }
                }
                // --- End Validation ---


                val categoryIdToSave: Long
                val goalIdToSave: Long?

                when (selectedItem) {
                    is Category -> {
                        categoryIdToSave = selectedItem.id
                        goalIdToSave = null
                    }
                    is Goal -> {
                        // We already checked goalContributionCategoryId is not null above
                        categoryIdToSave = currentState.goalContributionCategoryId!!
                        goalIdToSave = selectedItem.id
                    }
                    else -> {
                        // Should not happen due to initial check, but handle defensively
                        _uiState.update { it.copy(isSaving = false, isError = true) }
                        return@launch // Stop if item is somehow null or wrong type
                        // throw IllegalStateException("Selected item is not Category or Goal") // Alternative
                    }
                }

                val transactionToSave = Transaction(
                    id = currentState.transactionId ?: 0L, // Use 0L for new transaction
                    amount = amountDouble, // Use validated amount
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
                _uiState.update { state -> state.copy(isSaving = false) } // Use explicit 'state'
            } catch (e: Exception) {
                 // Log exception properly
                _uiState.update { state -> state.copy(isSaving = false, isError = true) } // Use explicit 'state'
            }
         }
     }
}
