package com.senlin.budgetmaster.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CategoryExpense(
    val categoryName: String,
    val totalAmount: BigDecimal,
    val color: Long // Store color as Long (packed ARGB)
)

data class ReportUiState(
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ReportViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Combine flows to get both transactions and categories
                repository.getAllTransactionsStream()
                    .combine(repository.getAllCategoriesStream()) { transactions, categories ->
                        Pair(transactions, categories)
                    }
                    .collect { (transactions, categories) ->
                        val categoryMap = categories.associateBy { it.id }
                        val expensesByCategory = transactions
                            .filter { it.amount < BigDecimal.ZERO } // Consider only expenses
                            .groupBy { it.categoryId }
                            .mapNotNull { (categoryId, transactionsInCategory) ->
                                val category = categoryMap[categoryId]
                                if (category != null) {
                                    val total = transactionsInCategory.sumOf { it.amount.abs() }
                                    CategoryExpense(
                                        categoryName = category.name,
                                        totalAmount = total,
                                        color = category.color // Use color from Category
                                    )
                                } else {
                                    // Handle transactions with no category or deleted category if necessary
                                    null
                                }
                            }
                            .sortedByDescending { it.totalAmount } // Sort for better visualization

                        _uiState.value = ReportUiState(
                            categoryExpenses = expensesByCategory,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = ReportUiState(isLoading = false, error = "Failed to load report data: ${e.message}")
            }
        }
    }
}
