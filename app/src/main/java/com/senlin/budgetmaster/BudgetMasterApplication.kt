package com.senlin.budgetmaster

import android.app.Application
import android.content.Context
import com.senlin.budgetmaster.data.db.AppDatabase
import com.senlin.budgetmaster.data.preferences.UserSettingsRepository
import com.senlin.budgetmaster.data.repository.BudgetRepository
import com.senlin.budgetmaster.data.repository.OfflineBudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class BudgetMasterApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer
        private set // Make setter private to prevent modification

    // Application-level coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        // Holds the current language code. Initialize with a sensible default or null.
        // This will be updated once the preference is loaded.
        var currentLanguageCode: String? = null
            // Allow setting from MainActivity during locale change process
            // private set // Ensure it's only set from within the Application class

        // Helper to create a ContextWrapper with a specific locale
        fun localeAwareContext(base: Context): Context {
            val lang = currentLanguageCode ?: Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "en"
            val locale = Locale(lang)
            val config = base.resources.configuration
            config.setLocale(locale)
            return base.createConfigurationContext(config)
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Launch a coroutine to load the initial language preference and then observe changes.
        // This ensures currentLanguageCode is set as early as possible.
        applicationScope.launch(Dispatchers.IO) { // Use IO dispatcher for DataStore
            // Try to get the initial value quickly.
            // runBlocking here is acceptable for a one-time initial fetch in Application.onCreate
            // if absolutely needed for synchronous access later, but observing is better.
            val initialLangCode = container.userSettingsRepository.languagePreference.firstOrNull()
            currentLanguageCode = initialLangCode ?: Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "en"

            // Continuously observe the language preference
            container.userSettingsRepository.languagePreference.collect { langCode ->
                currentLanguageCode = langCode ?: Locale.getDefault().language.takeIf { it.isNotBlank() } ?: "en"
                // Note: If the language changes while the app is running,
                // MainActivity's setLocale -> AppCompatDelegate.setApplicationLocales
                // should trigger an activity recreation.
            }
        }
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
            goalDao = database.goalDao(),
            userDao = database.userDao() // Added userDao
        )
    }
    // Lazily initialize UserSettingsRepository
    val userSettingsRepository: UserSettingsRepository by lazy {
        UserSettingsRepository(context)
    }
}
