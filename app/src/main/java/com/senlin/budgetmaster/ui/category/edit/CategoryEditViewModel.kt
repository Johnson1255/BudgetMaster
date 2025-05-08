package com.senlin.budgetmaster.ui.category.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CategoryUiState(
    val id: Long = 0L,
    val name: String = "",
    val isEntryValid: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val currentUserId: Long? = null // Keep track of userId
)

class CategoryEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BudgetRepository
) : ViewModel() {

    var categoryUiState by mutableStateOf(CategoryUiState())
        private set

    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L
    private var currentUserId: Long? = null // Store userId locally in VM

    // Call this from UI layer when userId is available
    fun initialize(userId: Long) {
        if (currentUserId == null) { // Prevent re-initialization
            currentUserId = userId
            // Initialize UI state with the userId, setting loading based on whether we need to fetch
            categoryUiState = CategoryUiState(currentUserId = userId, isLoading = (categoryId != 0L))
            if (categoryId != 0L) {
                loadCategory(userId, categoryId)
            }
        }
    }

    private fun loadCategory(userId: Long, categoryIdToLoad: Long) {
        viewModelScope.launch {
            // Ensure isLoading is true before starting fetch
            if (!categoryUiState.isLoading) {
                 categoryUiState = categoryUiState.copy(isLoading = true)
            }
            try {
                val category = repository.getCategoryById(categoryIdToLoad, userId)
                    .filterNotNull()
                    .first()
                // Convert using the correct extension function, passing userId
                categoryUiState = category.toCategoryUiState(userId, isEntryValid = true, isLoading = false)
            } catch (e: Exception) {
                categoryUiState = categoryUiState.copy(isLoading = false, error = "Failed to load category: ${e.message}")
            }
        }
    }

    fun updateUiState(newName: String) {
        categoryUiState = categoryUiState.copy(
            name = newName,
            isEntryValid = validateInput(newName)
        )
    }

    suspend fun saveCategory(): Boolean {
        val userId = currentUserId // Use the locally stored userId
        if (userId == null || userId == 0L) {
             categoryUiState = categoryUiState.copy(isLoading = false, error = "User not identified.")
             return false
        }
        if (!validateInput(categoryUiState.name)) {
            return false // Keep UI state as is, validation failed
        }
        categoryUiState = categoryUiState.copy(isLoading = true, error = null)
        return try {
            // Convert using the correct extension function, passing userId
            val category = categoryUiState.toCategory(userId)
            if (category.id == 0L) {
                repository.insertCategory(category) // userId is set within category object
            } else {
                repository.updateCategory(category) // userId is set within category object
            }
            categoryUiState = categoryUiState.copy(isLoading = false, isSaved = true)
            true
        } catch (e: Exception) {
            categoryUiState = categoryUiState.copy(isLoading = false, error = "Failed to save category: ${e.message}", isSaved = false)
            false
        }
    }

    private fun validateInput(name: String): Boolean {
        return name.isNotBlank()
    }
}

// Extension function to map Category data model to CategoryUiState
// Added userId parameter
fun Category.toCategoryUiState(userId: Long?, isEntryValid: Boolean = false, isLoading: Boolean = false): CategoryUiState = CategoryUiState(
    id = id,
    name = name,
    isEntryValid = isEntryValid,
    isLoading = isLoading,
    currentUserId = userId // Set userId in UI state
)

// Extension function to map CategoryUiState to Category data model
// Added userId parameter
fun CategoryUiState.toCategory(userId: Long): Category = Category(
    id = id,
    userId = userId, // Set userId from parameter
    name = name
)
