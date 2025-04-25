package com.senlin.budgetmaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.senlin.budgetmaster.navigation.Screen
import com.senlin.budgetmaster.ui.components.BottomNavigationBar // Import the bottom bar
import com.senlin.budgetmaster.ui.dashboard.DashboardScreen // Import the Dashboard screen
import com.senlin.budgetmaster.ui.theme.BudgetMasterTheme
import com.senlin.budgetmaster.ui.transaction.list.TransactionListScreen // Import the screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetMasterApp() // Call our main app composable
        }
    }
}

@Composable
fun BudgetMasterApp(navController: NavHostController = rememberNavController()) {
    BudgetMasterTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { BottomNavigationBar(navController = navController) } // Add the actual bottom bar
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route, // Start on Dashboard
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(modifier = Modifier) // Use the actual Dashboard screen
        }
        composable(Screen.Transactions.route) {
            TransactionListScreen(navController = navController) // Use the actual screen
        }
        composable(Screen.Goals.route) {
            // TODO: Replace with actual GoalsScreen composable
            PlaceholderScreen("Goals Screen")
        }
        composable(Screen.Reports.route) {
             // TODO: Replace with actual ReportsScreen composable
            PlaceholderScreen("Reports Screen")
        }
        composable(Screen.Categories.route) {
            // TODO: Replace with actual CategoriesScreen composable
            PlaceholderScreen("Categories Screen")
        }
        // TODO: Add composable routes for Add/Edit screens with arguments later
        // Example: composable(Screen.AddEditTransaction.withArgs(Screen.TRANSACTION_ID_ARG)) { ... }
    }
}

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
