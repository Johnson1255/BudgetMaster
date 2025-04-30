package com.senlin.budgetmaster

import android.app.Application
import android.content.Context
// Removed duplicate import
import com.senlin.budgetmaster.data.db.AppDatabase
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository // Import UserSettingsRepository
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.data.repository.OfflineBudgetRepository

class BudgetMasterApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
        private set // Make setter private to prevent modification

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Simple dependency injection container.
 * Holds instances of database, repository, and user settings.
 */
class AppContainer(private val context: Context) { // Make context a property if needed by multiple lazy initializers
    // Lazily initialize database and repository
    private val database by lazy { AppDatabase.getDatabase(context) }
    val budgetRepository: BudgetRepository by lazy {
        OfflineBudgetRepository(
            transactionDao = database.transactionDao(),
            categoryDao = database.categoryDao(),
            goalDao = database.goalDao()
        )
    }
    // Lazily initialize UserSettingsRepository
    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(context)
    }
}
