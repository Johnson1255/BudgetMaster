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
    val id: Long = 0L, // Changed to Long
    val name: String = "",
    val isEntryValid: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

class CategoryEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: BudgetRepository
) : ViewModel() {

    var categoryUiState by mutableStateOf(CategoryUiState())
        private set

    // Retrieve categoryId as Long? Default to 0L if null (for new category)
    private val categoryId: Long = savedStateHandle.get<Long>("categoryId") ?: 0L

    init {
        // Load category if categoryId is not 0 (i.e., editing existing)
        if (categoryId != 0L) {
            viewModelScope.launch {
                categoryUiState = categoryUiState.copy(isLoading = true)
                try {
                    // Use getCategoryById with Long ID
                    val category = repository.getCategoryById(categoryId)
                        .filterNotNull()
                        .first()
                    categoryUiState = category.toCategoryUiState(isEntryValid = true, isLoading = false)
                } catch (e: Exception) {
                    categoryUiState = categoryUiState.copy(isLoading = false, error = "Failed to load category: ${e.message}")
                }
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
        if (!validateInput(categoryUiState.name)) {
            return false
        }
        categoryUiState = categoryUiState.copy(isLoading = true, error = null)
        return try {
            val category = categoryUiState.toCategory()
            // Check against 0L for new category
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
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

// Extension functions to map between data model and UI state
fun Category.toCategoryUiState(isEntryValid: Boolean = false, isLoading: Boolean = false): CategoryUiState = CategoryUiState(
    id = id, // id is already Long in Category model
    name = name,
    isEntryValid = isEntryValid,
    isLoading = isLoading
)

fun CategoryUiState.toCategory(): Category = Category(
    id = id, // id is Long in CategoryUiState now
    name = name
)
