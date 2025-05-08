package com.senlin.budgetmaster.ui.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CategoryListUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true, // Start loading until userId is known
    val error: String? = null,
    val currentUserId: Long? = null // Track user ID
)

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryListViewModel(private val repository: BudgetRepository) : ViewModel() {

    // StateFlow for the current user ID - to be set by the UI layer
    private val _currentUserId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<CategoryListUiState> = _currentUserId.flatMapLatest { userId ->
        if (userId == null || userId == 0L) {
            // No valid user, return loading/empty state
            flowOf(CategoryListUiState(isLoading = true, currentUserId = null))
        } else {
            // Valid user, fetch categories
            repository.getAllCategories(userId)
                .map { categories ->
                    CategoryListUiState(
                        categories = categories,
                        isLoading = false,
                        currentUserId = userId
                    )
                }
                .catch { exception ->
                    // Emit error state
                    emit(CategoryListUiState(
                        isLoading = false,
                        error = "Failed to load categories: ${exception.message}",
                        currentUserId = userId
                    ))
                }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CategoryListUiState(isLoading = true) // Initial state is loading
    )

    fun setCurrentUserId(userId: Long?) {
        _currentUserId.value = userId
    }

    fun deleteCategory(category: Category) {
        // Ensure the category belongs to the current user before deleting
        if (category.userId == _currentUserId.value) {
            viewModelScope.launch {
                try {
                    repository.deleteCategory(category)
                    // StateFlow should automatically update due to the underlying Flow change
                } catch (e: Exception) {
                    // Optionally update UI state with a specific error message for deletion failure
                    // For now, the main flow's catch block might handle repository errors.
                    // Consider adding a specific deletion error state if needed.
                     println("Error deleting category: ${e.message}") // Log error
                }
            }
        } else {
             println("Error: Attempted to delete category belonging to another user.")
             // Optionally set an error message in the UI state
        }
    }
}
