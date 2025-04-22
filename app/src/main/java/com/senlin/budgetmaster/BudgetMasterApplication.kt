package com.senlin.budgetmaster

import android.app.Application
import com.senlin.budgetmaster.data.db.AppDatabase
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
 * Holds instances of database and repository.
 */
class AppContainer(context: Context) {
    // Lazily initialize database and repository
    private val database by lazy { AppDatabase.getDatabase(context) }
    val budgetRepository: BudgetRepository by lazy {
        OfflineBudgetRepository(
            database.transactionDao(),
            database.categoryDao(),
            database.goalDao()
        )
    }
}
