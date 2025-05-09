package com.senlin.budgetmaster.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    // Updated AddEditTransaction to handle optional ID for adding/editing
    object AddEditTransaction : Screen("addEditTransaction?${Screen.TRANSACTION_ID_ARG}={${Screen.TRANSACTION_ID_ARG}}") {
        // Use Long? for ID type, -1L for new transaction marker
        fun createRoute(transactionId: Long?) = "addEditTransaction?${Screen.TRANSACTION_ID_ARG}=${transactionId ?: -1L}"
        const val routeWithArg = "addEditTransaction?${Screen.TRANSACTION_ID_ARG}={${Screen.TRANSACTION_ID_ARG}}"
        val arguments = listOf(
            androidx.navigation.navArgument(Screen.TRANSACTION_ID_ARG) {
                type = androidx.navigation.NavType.LongType // Change type to LongType
                defaultValue = -1L // Default for adding new transaction (use Long)
            }
        )
    }
    object GoalList : Screen("goalList") // Renamed from Goals
    // Updated GoalEdit to handle optional ID for adding/editing
    object GoalEdit : Screen("goalEdit?${Screen.GOAL_ID_ARG}={${Screen.GOAL_ID_ARG}}") {
        fun createRoute(goalId: Long?) = "goalEdit?${Screen.GOAL_ID_ARG}=${goalId ?: -1L}" // Use -1L for new
        const val routeWithArg = "goalEdit?${Screen.GOAL_ID_ARG}={${Screen.GOAL_ID_ARG}}"
        val arguments = listOf(
            androidx.navigation.navArgument(Screen.GOAL_ID_ARG) {
                type = androidx.navigation.NavType.LongType
                defaultValue = -1L // Default for adding new goal
            }
        )
    }
    object CategoryList : Screen("categoryList") // Changed from Categories
    object CategoryEdit : Screen("categoryEdit/{categoryId}") {
        // Use Long? and 0L for adding, consistent with NavArgument defaultValue and Category ID type
        fun createRoute(categoryId: Long?) = "categoryEdit/${categoryId ?: 0L}"
    }
    // No separate CategoryAdd needed, handled by CategoryEdit with optional ID
    object Reports : Screen("reports")
    object Settings : Screen("settings") // Added Settings screen route

    // Helper function to create routes with arguments
    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/{$arg}")
            }
        }
    }

     // Helper function to create actual navigation path with values
    fun withArgsValues(vararg values: Any): String {
        var routeWithPath = route
        values.forEach { value ->
            // Basic replacement, assumes order matches definition
            // A more robust solution might involve named arguments
             routeWithPath = routeWithPath.replaceFirst(Regex("\\{[^}]+\\}"), value.toString())
        }
        // If the route template still contains placeholders (e.g., optional args not provided),
        // we might need to clean them up or handle them differently depending on the nav library specifics.
        // For now, let's assume required args are always provided.
        // Example cleanup for trailing optional args: routeWithPath.replace(Regex("/\\{[^}]+\\}$"), "")
        return routeWithPath
    }

    // Define argument keys (optional but good practice)
    companion object {
        const val TRANSACTION_ID_ARG = "transactionId"
        const val GOAL_ID_ARG = "goalId" // Ensure this matches the key in the route "goalEdit/{goalId}"
        const val CATEGORY_ID_ARG = "categoryId" // Key for category editing
    }
}
