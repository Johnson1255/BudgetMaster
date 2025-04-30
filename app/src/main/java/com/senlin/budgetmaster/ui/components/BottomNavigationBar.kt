package com.senlin.budgetmaster.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet // Keep existing specific imports if needed elsewhere
import androidx.compose.material.icons.filled.Assessment // Keep existing specific imports if needed elsewhere
import androidx.compose.material.icons.filled.Category // Import Category icon
import androidx.compose.material.icons.filled.Dashboard // Keep existing specific imports if needed elsewhere
import androidx.compose.material.icons.filled.Savings // Keep existing specific imports if needed elsewhere
import androidx.compose.material.icons.filled.Settings // Import Settings icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.senlin.budgetmaster.navigation.Screen

// Data class to represent each navigation item
data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Dashboard", Icons.Filled.Dashboard, Screen.Dashboard.route),
        BottomNavItem("Transactions", Icons.Filled.AccountBalanceWallet, Screen.Transactions.route),
        BottomNavItem("Goals", Icons.Filled.Savings, Screen.GoalList.route), // Updated route
        BottomNavItem("Categories", Icons.Filled.Category, Screen.CategoryList.route), // Added Categories item
        BottomNavItem("Reports", Icons.Filled.Assessment, Screen.Reports.route),
        BottomNavItem("Settings", Icons.Filled.Settings, Screen.Settings.route) // Added Settings item
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
