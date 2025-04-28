package com.senlin.budgetmaster.ui.category.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class CategoryListUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CategoryListViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryListUiState(isLoading = true))
    val uiState: StateFlow<CategoryListUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAllCategoriesStream()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load categories: ${exception.message}"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                // No need to manually update state, Flow should emit the new list
            } catch (e: Exception) {
                 _uiState.value = _uiState.value.copy(
                    error = "Failed to delete category: ${e.message}"
                )
            }
        }
    }
}
