package com.senlin.budgetmaster.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.ui.ViewModelFactory // Use ViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory.Factory) // Use correct factory
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            DashboardContent(uiState = uiState)
        }
    }
}

@Composable
fun DashboardContent(uiState: DashboardUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BalanceCard(balance = uiState.balance)
        }

        item {
            SectionTitle("Recent Transactions")
        }
        if (uiState.recentTransactions.isEmpty()) {
            item { Text("No recent transactions.") }
        } else {
            items(uiState.recentTransactions) { transaction ->
                TransactionItem(transaction = transaction)
                HorizontalDivider() // Add a divider between items
            }
        }


        item {
            SectionTitle("Saving Goals")
        }
        if (uiState.goals.isEmpty()) {
            item { Text("No saving goals set.") }
        } else {
            items(uiState.goals) { goal ->
                GoalItem(goal = goal)
                HorizontalDivider() // Add a divider between items
            }
        }
    }
}

@Composable
fun BalanceCard(balance: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Balance",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(balance),
                style = MaterialTheme.typography.headlineLarge,
                color = if (balance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp) // Add some vertical padding
    )
}


@Composable
fun TransactionItem(transaction: Transaction, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note ?: "Transaction", // Use note or default text
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatDate(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatCurrency(transaction.amount),
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun GoalItem(goal: Goal, modifier: Modifier = Modifier) {
    // Basic display for a goal - can be enhanced later
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = goal.name,
            style = MaterialTheme.typography.bodyLarge
        )
        // TODO: Add progress visualization later
        Text(
            text = "${formatCurrency(goal.currentAmount)} / ${formatCurrency(goal.targetAmount)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


// --- Helper Functions ---

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance() // Use default locale currency
    // You might want to customize the locale: NumberFormat.getCurrencyInstance(Locale("es", "CO")) for COP
    return format.format(amount)
}

fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}
