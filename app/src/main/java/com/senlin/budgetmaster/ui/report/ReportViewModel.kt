package com.senlin.budgetmaster.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senlin.budgetmaster.data.model.Category // Keep if needed for category mapping
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType // Import TransactionType
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository // Added
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull // Added
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update // Import update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CategoryExpense(
    val categoryName: String,
    val totalAmount: Double
)

// Data class for daily expense trend
data class DailyExpense(
    val date: LocalDate,
    val totalAmount: Double
)

// Enum to define the different types of reports available
enum class ReportType {
    EXPENSE_BY_CATEGORY,
    INCOME_VS_EXPENSE,
    SPENDING_TREND // Add spending trend type
}

data class ReportUiState(
    val selectedReportType: ReportType = ReportType.EXPENSE_BY_CATEGORY, // Add selected type
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val dailyExpenses: List<DailyExpense> = emptyList(), // Add daily expenses for trend
    val totalIncome: Double = 0.0, // Add total income
    val totalExpense: Double = 0.0, // Add total expense
    val startDate: LocalDate = YearMonth.now().atDay(1), // Default to start of current month
    val endDate: LocalDate = YearMonth.now().atEndOfMonth(), // Default to end of current month
    val isLoading: Boolean = true,
    val error: String? = null,
    val csvContentToShare: String? = null // Add field for CSV export trigger
)

class ReportViewModel(
    private val repository: BudgetRepository,
    private val userSettingsRepository: UserSettingsRepository // Added
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        // Load data for the default date range on init
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            val userId = userSettingsRepository.currentUserId.firstOrNull()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "User not logged in.") }
                return@launch
            }

            // Get current date range from state
            val currentStartDate = _uiState.value.startDate
            val currentEndDate = _uiState.value.endDate
            _uiState.update { it.copy(isLoading = true, error = null) } // Start loading, clear previous error
            try {
                // Combine flows to get transactions within the date range and all categories
                repository.getTransactionsBetweenDates(userId, currentStartDate, currentEndDate) // Fetch based on date range
                    .combine(repository.getAllCategories(userId)) { transactions, categories -> // Explicit parameter names
                        Pair(transactions, categories)
                    }
                    .collect { (transactionsResult, categoriesResult) -> // Use distinct names for collected values
                        // Calculate total income and expense
                        var calculatedTotalIncome = 0.0
                        var calculatedTotalExpense = 0.0
                        transactionsResult.forEach { transaction ->
                            when (transaction.type) {
                                TransactionType.INCOME -> calculatedTotalIncome += transaction.amount
                                TransactionType.EXPENSE -> calculatedTotalExpense += transaction.amount
                            }
                        }

                        // Calculate expenses per day for the trend chart
                        val expensesByDay = transactionsResult
                            .filter { it.type == TransactionType.EXPENSE }
                            .groupBy { it.date } // Group by LocalDate directly
                            .map { (date, transactionsOnDay) ->
                                DailyExpense(
                                    date = date, // Date is already LocalDate
                                    totalAmount = transactionsOnDay.sumOf { it.amount } // Sum amounts for the day
                                )
                            }
                            .sortedBy { it.date } // Sort by date for the line chart

                        val categoryMap = categoriesResult.associateBy { category -> category.id } // Explicit lambda parameter
                        val expensesByCategory = transactionsResult // Use the collected transactions
                            .filter { transaction -> transaction.type == TransactionType.EXPENSE } // Use imported TransactionType
                            .groupBy { transaction -> transaction.categoryId } // Explicit lambda parameter
                            .mapNotNull { (categoryId, transactionsInCategory) ->
                                val category = categoryMap[categoryId]
                                if (category != null) {
                                    // Sum Doubles using fold. Amount is stored as positive.
                                    val total = transactionsInCategory.fold(0.0) { acc, transaction ->
                                        acc + transaction.amount
                                    }
                                    CategoryExpense(
                                        categoryName = category.name,
                                        totalAmount = total
                                    )
                                } else {
                                    // Handle transactions with no category or deleted category if necessary
                                    null
                                }
                            }
                            .sortedByDescending { categoryExpense -> categoryExpense.totalAmount } // Explicit lambda parameter

                        // Update state with fetched data
                        // Update state with fetched data
                        _uiState.update { currentState ->
                            currentState.copy(
                                categoryExpenses = expensesByCategory,
                                dailyExpenses = expensesByDay, // Update daily expenses
                                totalIncome = calculatedTotalIncome, // Update total income
                                totalExpense = calculatedTotalExpense, // Update total expense
                                isLoading = false
                                // selectedReportType, startDate and endDate are preserved
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { currentState -> // Explicit lambda parameter
                    currentState.copy(isLoading = false, error = "Failed to load report data: ${e.message}")
                }
            }
        }
    }

    /**
     * Updates the selected date range and triggers reloading of the report data.
     */
    fun updateDateRange(newStartDate: LocalDate, newEndDate: LocalDate) {
        _uiState.update { currentState -> // Explicit lambda parameter
            currentState.copy(startDate = newStartDate, endDate = newEndDate)
        }
        // Reload data with the new date range
        loadReportData()
    }

    /**
     * Updates the currently selected report type in the UI state.
     */
    fun updateSelectedReportType(newReportType: ReportType) {
        _uiState.update { currentState ->
            currentState.copy(selectedReportType = newReportType)
        }
        // No need to reload data here, as all necessary data is already loaded.
        // The UI will react to the change in selectedReportType.
    }

    /**
     * Generates CSV content based on the current report state and updates UI state to trigger sharing.
     */
    fun prepareCsvExport() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.error != null) return // Don't export if loading or error

        val csvBuilder = StringBuilder()
        val header: String
        val dataRows: List<String>

        // Add Report Title and Date Range
        csvBuilder.appendLine("BudgetMaster Report")
        csvBuilder.appendLine("Period: ${currentState.startDate} to ${currentState.endDate}")
        csvBuilder.appendLine() // Blank line

        when (currentState.selectedReportType) {
            ReportType.EXPENSE_BY_CATEGORY -> {
                header = "Category,Amount"
                dataRows = currentState.categoryExpenses.map { "${it.categoryName},${it.totalAmount}" }
            }
            ReportType.INCOME_VS_EXPENSE -> {
                header = "Item,Amount"
                dataRows = listOf(
                    "Total Income,${currentState.totalIncome}",
                    "Total Expense,${currentState.totalExpense}",
                    "Net ${if (currentState.totalIncome >= currentState.totalExpense) "Income" else "Loss"},${currentState.totalIncome - currentState.totalExpense}"
                )
            }
            ReportType.SPENDING_TREND -> {
                header = "Date,Amount"
                dataRows = currentState.dailyExpenses.map { "${it.date},${it.totalAmount}" }
            }
        }

        csvBuilder.appendLine(header)
        dataRows.forEach { csvBuilder.appendLine(it) }

        _uiState.update { it.copy(csvContentToShare = csvBuilder.toString()) }
    }

    /**
     * Resets the CSV export trigger in the UI state. Should be called after handling the share action.
     */
    fun clearCsvExportTrigger() {
        _uiState.update { it.copy(csvContentToShare = null) }
    }
}
