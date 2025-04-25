package com.senlin.budgetmaster.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Transactions : Screen("transactions")
    object AddEditTransaction : Screen("add_edit_transaction") // Can take an optional transactionId argument
    object GoalList : Screen("goalList") // Renamed from Goals
    object GoalEdit : Screen("goalEdit/{goalId}") { // Renamed and corrected route
        fun createRoute(goalId: Long) = "goalEdit/$goalId"
    }
    object Categories : Screen("categories") // For managing categories
    object Reports : Screen("reports")

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
    }
}
