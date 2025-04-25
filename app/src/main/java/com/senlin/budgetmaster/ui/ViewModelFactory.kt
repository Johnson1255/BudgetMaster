package com.senlin.budgetmaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.senlin.budgetmaster.BudgetMasterApplication
import com.senlin.budgetmaster.data.repository.BudgetRepository

/**
 * Factory for creating ViewModels with dependencies.
 */
object AppViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            // Get the application instance from CreationExtras
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as BudgetMasterApplication

            // Get the repository from the application's container
            val budgetRepository = application.container.budgetRepository

            // Create the requested ViewModel instance
            // TODO: Add cases for each ViewModel as we create them
            if (modelClass.isAssignableFrom(com.senlin.budgetmaster.ui.transaction.list.TransactionListViewModel::class.java)) {
                  @Suppress("UNCHECKED_CAST")
                  return com.senlin.budgetmaster.ui.transaction.list.TransactionListViewModel(budgetRepository) as T
             }
             if (modelClass.isAssignableFrom(com.senlin.budgetmaster.ui.dashboard.DashboardViewModel::class.java)) {
                 @Suppress("UNCHECKED_CAST")
                 return com.senlin.budgetmaster.ui.dashboard.DashboardViewModel(budgetRepository) as T
             }
             /*
             if (modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java)) {
                 @Suppress("UNCHECKED_CAST")
                 return AddEditTransactionViewModel(budgetRepository) as T
            }
            // Add other ViewModel cases here...
            */

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/**
 * Extension function to easily get the repository from CreationExtras.
 */
fun CreationExtras.budgetMasterRepository(): BudgetRepository {
    val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as BudgetMasterApplication
    return application.container.budgetRepository
}
