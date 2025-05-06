package com.senlin.budgetmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box // Import Box for centering
import androidx.compose.material3.CircularProgressIndicator // Import loading indicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Import Alignment for centering
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp // Import dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType // Import NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument // Import navArgument
import com.senlin.budgetmaster.navigation.Screen
import com.senlin.budgetmaster.ui.components.BottomNavigationBar // Import the bottom bar
import com.senlin.budgetmaster.ui.dashboard.DashboardScreen // Import the Dashboard screen
import com.senlin.budgetmaster.ui.goal.edit.GoalEditScreen // Import Goal Edit Screen
import com.senlin.budgetmaster.ui.goal.list.GoalListScreen // Import Goal List Screen
import com.senlin.budgetmaster.ui.report.ReportScreen // Import Report Screen
import com.senlin.budgetmaster.ui.category.list.CategoryListScreen // Import Category List Screen
import com.senlin.budgetmaster.ui.category.edit.CategoryEditScreen // Import Category Edit Screen
import com.senlin.budgetmaster.ui.splash.SplashScreen // Import Splash Screen
import com.senlin.budgetmaster.ui.theme.BudgetMasterTheme
import com.senlin.budgetmaster.ui.transaction.edit.TransactionEditScreen // Import Transaction Edit Screen
import com.senlin.budgetmaster.ui.transaction.list.TransactionListScreen // Import the screen
import com.senlin.budgetmaster.ui.settings.SettingsScreen // Import Settings Screen
import com.senlin.budgetmaster.ui.MainViewModel // Import MainViewModel
import com.senlin.budgetmaster.BudgetMasterApplication // Import Application class
import android.content.Context // Import Context for setLocale
import android.util.Log // Import Log for debugging
import android.content.res.Configuration // Import Configuration for setLocale
import androidx.appcompat.app.AppCompatDelegate // Import AppCompatDelegate for locale setting
import androidx.core.os.LocaleListCompat // Import LocaleListCompat for locale setting
import com.senlin.budgetmaster.ui.ViewModelFactory // Import ViewModelFactory
import kotlinx.coroutines.launch // Import launch
import java.util.Locale // Import Locale for setLocale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply the locale from BudgetMasterApplication before the activity is fully created
        super.attachBaseContext(BudgetMasterApplication.localeAwareContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetMasterApp() // Call our main app composable
        }
    }
}

@Composable
fun BudgetMasterApp(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(factory = ViewModelFactory.Factory) // Get MainViewModel
) {
    val mainUiState by mainViewModel.uiState.collectAsState() // Collect state
    val context = LocalContext.current // Get context for setLocale

    // Effect to update locale when language preference changes
    LaunchedEffect(mainUiState.selectedLanguageCode) {
        Log.d("LocaleChange", "LaunchedEffect triggered. Selected language code: ${mainUiState.selectedLanguageCode}")
        mainUiState.selectedLanguageCode?.let { code ->
            Log.d("LocaleChange", "Calling setLocale with code: $code from LaunchedEffect")
            // Always attempt to set the locale.
            // AppCompatDelegate.setApplicationLocales() should handle
            // whether a configuration change (and activity recreation) is actually needed.
            setLocale(context, code)
        }
    }

    BudgetMasterTheme {
        val currentBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                // Only show bottom bar if not on the splash screen
                if (currentRoute != Screen.Splash.route) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            // Determine start destination based on ViewModel state
            val startDestination = when (mainUiState.initialLanguageSet) {
                true -> Screen.Dashboard.route // Language set, go to Dashboard
                false -> Screen.Splash.route   // Language not set, go to Splash
                null -> null // Still loading preference
            }

            if (startDestination != null) {
                AppNavHost(
                    navController = navController,
                    startDestination = startDestination, // Pass the determined start destination
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                // Show a loading indicator while checking preference
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String, // Receive start destination
    modifier: Modifier = Modifier
    // Removed repository injection here, will get it inside composable where needed or via ViewModel
) {
    // Get repository instance here if needed directly, or rely on ViewModels
    val userSettingsRepository = (LocalContext.current.applicationContext as BudgetMasterApplication).container.userSettingsRepository
    val scope = rememberCoroutineScope() // Coroutine scope for saving preference

    NavHost(
        navController = navController,
        startDestination = startDestination, // Use the determined start destination
        modifier = modifier
    ) { // Ensure this lambda structure is correct
        composable(Screen.Splash.route) { // Correct composable definition
            SplashScreen(
                onLanguageSelected = { languageCode -> // Correct lambda usage
                    scope.launch { // Correct coroutine launch
                        userSettingsRepository.saveLanguagePreference(languageCode)
                        // TODO: Update app locale if necessary (requires more complex setup)
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true } // Remove splash from back stack
                        }
                    }
                }
            )
        } // End composable(Screen.Splash.route)

        composable(Screen.Dashboard.route) { // Correct composable definition
            DashboardScreen(modifier = Modifier) // Use the actual Dashboard screen
        } // End composable(Screen.Dashboard.route)

        composable(Screen.Transactions.route) { // Correct composable definition
            TransactionListScreen(navController = navController) // Use the actual screen
        } // End composable(Screen.Transactions.route)

        // Use GoalList route and screen
        composable(Screen.GoalList.route) { // Correct composable definition
            GoalListScreen(navController = navController)
        } // End composable(Screen.GoalList.route)

        composable(Screen.Reports.route) { // Correct composable definition
            ReportScreen(modifier = Modifier) // Use the actual Report screen
        } // End composable(Screen.Reports.route)

        // Replace Placeholder with actual Category List Screen
        composable(Screen.CategoryList.route) { // Correct composable definition
            CategoryListScreen(
                onAddCategoryClick = {
                    navController.navigate(Screen.CategoryEdit.createRoute(null)) // Navigate to add screen
                },
                onEditCategoryClick = { categoryId ->
                    navController.navigate(Screen.CategoryEdit.createRoute(categoryId)) // Navigate to edit screen
                }
            )
        } // End composable(Screen.CategoryList.route)

        // Add Category Edit Screen route with argument
        composable( // Correct composable definition
            route = Screen.CategoryEdit.route,
            arguments = listOf(navArgument(Screen.CATEGORY_ID_ARG) {
                type = NavType.LongType // Use LongType for category ID
                defaultValue = 0L // Default 0L for adding new category (align with ViewModel)
            })
        ) { backStackEntry -> // Receive backStackEntry
             CategoryEditScreen(
                 navigateBack = { navController.popBackStack() },
                 onSaveComplete = { navController.popBackStack() }
                 // ViewModel will get ID from backStackEntry.arguments or SavedStateHandle
             )
        } // End composable(Screen.CategoryEdit.route)

        // Add Goal Edit Screen route with optional argument
        composable( // Correct composable definition
            route = Screen.GoalEdit.routeWithArg, // Use routeWithArg
            arguments = Screen.GoalEdit.arguments // Use arguments from Screen object
        ) { backStackEntry -> // Receive backStackEntry
            // Pass navController for back navigation, ViewModel handles ID retrieval
            GoalEditScreen(navController = navController)
        } // End composable(Screen.GoalEdit.route)

        // Add Transaction Edit Screen route with optional argument
        composable( // Correct composable definition
            route = Screen.AddEditTransaction.routeWithArg,
            arguments = Screen.AddEditTransaction.arguments
        ) { backStackEntry -> // Receive backStackEntry
            TransactionEditScreen(
                navigateBack = { navController.popBackStack() }
                 // ViewModel will get the transactionId from SavedStateHandle via backStackEntry
            )
        } // End composable(Screen.AddEditTransaction.route)

        composable(Screen.Settings.route) { // Add Settings Screen route
            SettingsScreen() // Use the actual Settings screen
        } // End composable(Screen.Settings.route)
    } // End NavHost
} // End AppNavHost

// Simple placeholder for screens not yet implemented
@Composable
fun PlaceholderScreen(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Screen: $name",
        modifier = modifier.padding(16.dp) // Add some padding
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BudgetMasterApp() // Preview the whole app structure
}

// Helper function to update the app's locale
private fun setLocale(context: Context, languageCode: String) {
    Log.d("LocaleChange", "setLocale function called. languageCode: $languageCode")

    // Check 1: Current context's configuration.
    // This should be updated by attachBaseContext after the activity is recreated.
    val currentContextLocale = context.resources.configuration.locales.get(0)
    Log.d("LocaleChange", "Current context locale from resources.configuration: ${currentContextLocale.toLanguageTag()}")

    if (currentContextLocale.language == languageCode) {
        Log.d("LocaleChange", "Context already has target locale $languageCode. Verifying AppCompatDelegate persistence.")
        // Even if context is correct, ensure AppCompatDelegate is aligned for persistence.
        val appCompatLocales = AppCompatDelegate.getApplicationLocales()
        if (!appCompatLocales.isEmpty && appCompatLocales.get(0)?.language == languageCode) {
            Log.d("LocaleChange", "AppCompatDelegate also aligned for $languageCode. No further action needed by setLocale.")
            return // Both context and AppCompatDelegate are aligned.
        }
        Log.d("LocaleChange", "AppCompatDelegate not aligned (${appCompatLocales.toLanguageTags()}) or empty. Will proceed to set via AppCompatDelegate, but won't recreate if context is already correct.")
        // If context is correct but AppCompatDelegate isn't, we still call setApplicationLocales
        // but avoid recreation if possible, as the visual locale is already applied.
        // However, for simplicity and to ensure AppCompatDelegate persistence is robustly triggered,
        // we might still proceed with the full logic if this state is encountered.
        // For now, if context is correct, we assume the main loop-causing issue is resolved.
        // If AppCompatDelegate.getApplicationLocales() *still* shows empty later, that's a separate persistence concern.
        // The primary goal here is to stop the immediate recreation loop.
        // If the context is already correct, we might not need to recreate.
        // However, setApplicationLocales is for persistence.
        // Let's ensure it's called if needed, but prioritize breaking the loop.
        if (appCompatLocales.isEmpty || appCompatLocales.get(0)?.language != languageCode) {
            Log.d("LocaleChange", "Context is $languageCode, but AppCompatDelegate needs update. Setting AppCompatDelegate without forcing recreate from here if context is already fine.")
            BudgetMasterApplication.currentLanguageCode = languageCode // Ensure app var is also aligned
            val appLocaleListForPersistence: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocaleListForPersistence)
            // Do not recreate here if currentContextLocale.language == languageCode,
            // as the UI should already reflect it.
            return
        }
        return // Context has the language, and AppCompatDelegate is also aligned.
    }

    Log.d("LocaleChange", "Context locale is ${currentContextLocale.language}, target is $languageCode. Proceeding to set locale fully.")

    // Update the Application's current language code
    // This ensures attachBaseContext uses the new code upon recreation
    BudgetMasterApplication.currentLanguageCode = languageCode

    val locale = Locale(languageCode)
    Locale.setDefault(locale) // Set default locale for the JVM (good practice)

    // Update app configuration via AppCompatDelegate for persistence and system awareness
    val appLocaleList: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
    Log.d("LocaleChange", "Calling AppCompatDelegate.setApplicationLocales with language tags: ${appLocaleList.toLanguageTags()}")
    AppCompatDelegate.setApplicationLocales(appLocaleList)
    Log.d("LocaleChange", "AppCompatDelegate.setApplicationLocales finished for $languageCode.")

    // Explicitly recreate the activity to apply the new locale
    // This is generally needed for the changes from setApplicationLocales to take full effect immediately.
    if (context is ComponentActivity) {
        Log.d("LocaleChange", "Recreating activity to apply locale changes.")
        context.recreate()
    } else {
        Log.w("LocaleChange", "Context is not an Activity, cannot call recreate(). Locale might not apply immediately.")
    }
}
