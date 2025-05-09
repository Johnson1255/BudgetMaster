package com.senlin.budgetmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box // Import Box for centering
import androidx.compose.material.icons.Icons // Import Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import Back Arrow
import androidx.compose.material.icons.filled.Settings // Import Settings icon for TopAppBar
import androidx.compose.material3.CircularProgressIndicator // Import loading indicator
import androidx.compose.material3.ExperimentalMaterial3Api // For TopAppBar
import androidx.compose.material3.Icon // For IconButton
import androidx.compose.material3.IconButton // For TopAppBar actions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar // Import TopAppBar
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
import com.senlin.budgetmaster.ui.auth.AuthViewModel // Import AuthViewModel
import com.senlin.budgetmaster.ui.auth.LoginScreen // Import LoginScreen
import com.senlin.budgetmaster.ui.auth.RegisterScreen // Import RegisterScreen
import com.senlin.budgetmaster.ui.theme.BudgetMasterTheme
import com.senlin.budgetmaster.ui.transaction.edit.TransactionEditScreen // Import Transaction Edit Screen
import com.senlin.budgetmaster.ui.transaction.list.TransactionListScreen // Import the screen
import com.senlin.budgetmaster.ui.settings.SettingsScreen // Import Settings Screen
import com.senlin.budgetmaster.ui.MainViewModel // Import MainViewModel
import com.senlin.budgetmaster.BudgetMasterApplication // Import Application class
import android.content.Context // Import Context for setLocale
import android.util.Log // Import Log for debugging
import android.content.res.Configuration // Import Configuration for setLocale
import androidx.compose.ui.res.stringResource // For string resources
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

        val showBars = currentRoute !in listOf(
            Screen.Splash.route,
            Screen.Login.route,
            Screen.Register.route
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showBars) {
                    AppTopBar(navController = navController, currentRoute = currentRoute)
                }
            },
            bottomBar = {
                if (showBars) {
                    BottomNavigationBar(navController = navController)
                }
            }
        ) { innerPadding ->
            // Determine start destination based on ViewModel state
            val startDestination = when {
                mainUiState.isLoading -> null // Still loading preference from MainViewModel
                mainUiState.initialLanguageSet == false -> Screen.Splash.route
                mainUiState.currentUserId == null -> Screen.Login.route // Language set, but no user logged in
                else -> Screen.Dashboard.route // Language set and user logged in
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
    val mainViewModel: MainViewModel = viewModel(factory = ViewModelFactory.Factory) // Get MainViewModel for auth state
    val authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.Factory) // Get AuthViewModel
    val scope = rememberCoroutineScope() // Coroutine scope for saving preference

    NavHost(
        navController = navController,
        startDestination = startDestination, // Use the determined start destination
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onLanguageSelected = { languageCode ->
                    scope.launch {
                        userSettingsRepository.saveLanguagePreference(languageCode)
                        // After language selection, check auth state to navigate appropriately
                        val currentUserId = mainViewModel.uiState.value.currentUserId
                        val nextRoute = if (currentUserId == null) Screen.Login.route else Screen.Dashboard.route
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true // Avoid multiple dashboard instances
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        popUpTo(Screen.Login.route) { inclusive = true } // Also pop Login if it's in backstack
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(modifier = Modifier)
        }

        composable(Screen.Transactions.route) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(navController: NavHostController, currentRoute: String?) {
    // Don't show the main TopAppBar content if on the Report screen, as it has its own.
    if (currentRoute == Screen.Reports.route) {
        // Render an empty TopAppBar or nothing to avoid overlap/double elements
        // TopAppBar(title = {}) // Option 1: Empty TopAppBar
        return // Option 2: Don't render this TopAppBar at all for Reports screen
    }

    // Determine the title based on the current route (excluding Reports)
    val title = when (currentRoute) {
        Screen.Dashboard.route -> stringResource(id = R.string.dashboard_title)
        Screen.Transactions.route -> stringResource(id = R.string.transactions_title)
        Screen.GoalList.route -> stringResource(id = R.string.goals_title)
        Screen.CategoryList.route -> stringResource(id = R.string.categories_title)
        Screen.Settings.route -> stringResource(id = R.string.settings_title)
        // Add other screens here
        else -> stringResource(id = R.string.app_name) // Default title
    }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            // Show back arrow only on Settings screen (as other screens are top-level)
            if (currentRoute == Screen.Settings.route) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigate_back_description) // Re-use existing or add specific
                    )
                }
            }
            // Add other navigation icons for specific screens if needed later
        },
        actions = {
            // Show settings icon on screens managed by this TopAppBar, except Settings itself
            if (currentRoute != Screen.Settings.route) { // Already excludes Reports route implicitly by the check at the start
                IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.settings_action_description)
                    )
                }
            }
        }
    )
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
