package com.senlin.budgetmaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle // Import for SavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer // Import initializer
import androidx.lifecycle.viewmodel.viewModelFactory // Import viewModelFactory
import com.senlin.budgetmaster.BudgetMasterApplication
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.ui.dashboard.DashboardViewModel
import com.senlin.budgetmaster.ui.goal.edit.GoalEditViewModel // Import GoalEditViewModel
import com.senlin.budgetmaster.ui.goal.list.GoalListViewModel // Import GoalListViewModel
import com.senlin.budgetmaster.ui.report.ReportViewModel // Import ReportViewModel
import com.senlin.budgetmaster.ui.transaction.list.TransactionListViewModel
import com.senlin.budgetmaster.ui.category.list.CategoryListViewModel // Import CategoryListViewModel
import com.senlin.budgetmaster.ui.category.edit.CategoryEditViewModel // Import CategoryEditViewModel
import com.senlin.budgetmaster.ui.transaction.edit.TransactionEditViewModel // Import TransactionEditViewModel
import com.senlin.budgetmaster.ui.MainViewModel // Import MainViewModel
import com.senlin.budgetmaster.ui.settings.SettingsViewModel // Import SettingsViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire BudgetMaster app
 */
object ViewModelFactory {

    val Factory = viewModelFactory {
        // Initializer for DashboardViewModel
        initializer {
            DashboardViewModel(budgetMasterApplication().container.budgetRepository)
        }

        // Initializer for TransactionListViewModel
        initializer {
            TransactionListViewModel(budgetMasterApplication().container.budgetRepository)
        }

        // Initializer for GoalListViewModel
        initializer {
            GoalListViewModel(budgetMasterApplication().container.budgetRepository)
        }

        // Initializer for GoalEditViewModel
        initializer {
            GoalEditViewModel(
                this.createSavedStateHandle(), // Provides SavedStateHandle
                budgetMasterApplication().container.budgetRepository
            )
        }

        // Initializer for ReportViewModel
        initializer {
            ReportViewModel(budgetMasterApplication().container.budgetRepository)
        }

        // Initializer for CategoryListViewModel
        initializer {
            CategoryListViewModel(budgetMasterApplication().container.budgetRepository)
        }

        // Initializer for CategoryEditViewModel
        initializer {
            CategoryEditViewModel(
                this.createSavedStateHandle(), // Provides SavedStateHandle
                budgetMasterApplication().container.budgetRepository
            )
        }

        // Initializer for TransactionEditViewModel
        initializer {
            TransactionEditViewModel(
                budgetMasterApplication().container.budgetRepository,
                 this.createSavedStateHandle() // Provides SavedStateHandle
             )
         }

        // Initializer for MainViewModel
        initializer {
            MainViewModel(budgetMasterApplication().container.userSettingsRepository)
        }

        // Initializer for SettingsViewModel
        initializer {
            SettingsViewModel(budgetMasterApplication().container.userSettingsRepository)
        }
     }
 }

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [BudgetMasterApplication].
 */
fun CreationExtras.budgetMasterApplication(): BudgetMasterApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BudgetMasterApplication)


// --- Old Factory Implementation (commented out or remove) ---
/*
object AppViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as BudgetMasterApplication
            val budgetRepository = application.container.budgetRepository

            if (modelClass.isAssignableFrom(TransactionListViewModel::class.java)) {
                  @Suppress("UNCHECKED_CAST")
                  return TransactionListViewModel(budgetRepository) as T
             }
             if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                 @Suppress("UNCHECKED_CAST")
                 return DashboardViewModel(budgetRepository) as T
             }
             if (modelClass.isAssignableFrom(GoalListViewModel::class.java)) {
                 @Suppress("UNCHECKED_CAST")
                 return GoalListViewModel(budgetRepository) as T
             }
             if (modelClass.isAssignableFrom(GoalEditViewModel::class.java)) {
                 // GoalEditViewModel requires SavedStateHandle, which is tricky to get here directly.
                 // Using the newer viewModelFactory {} DSL is recommended.
                 // For now, let's throw an error or handle it if possible with older methods.
                 // This highlights the benefit of the newer factory approach.
                 // We'll assume the composable provides it correctly via viewModel() call.
                 // This old factory style doesn't easily support SavedStateHandle injection.
                 // Consider migrating fully to the viewModelFactory DSL.
                 // For demonstration, we might try a basic instantiation if no SavedStateHandle needed,
                 // but GoalEditViewModel DOES need it.
                 // Let's add it assuming the call site handles SavedStateHandle injection:
                 // This part is problematic with the old Factory style.
                 // The viewModelFactory DSL handles this automatically.
                 // We will replace this whole object with the DSL approach.
                 throw IllegalArgumentException("GoalEditViewModel requires SavedStateHandle, use viewModelFactory DSL")

                 //@Suppress("UNCHECKED_CAST")
                 //return GoalEditViewModel(???, budgetRepository) as T // How to get SavedStateHandle here?
             }
             /*
             if (modelClass.isAssignableFrom(AddEditTransactionViewModel::class.java)) {
                 @Suppress("UNCHECKED_CAST")
                 return AddEditTransactionViewModel(budgetRepository) as T
            }
            */

            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
*/
