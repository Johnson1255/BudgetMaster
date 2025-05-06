package com.senlin.budgetmaster.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // Import size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons // Import Icons
import androidx.compose.material.icons.filled.ArrowDownward // Import specific icons
import androidx.compose.material.icons.filled.ArrowUpward // Import specific icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api // Import for TopAppBar
import androidx.compose.material3.HorizontalDivider // Keep for potential future use, but removing from lists
import androidx.compose.material3.Icon // Import Icon
import androidx.compose.material3.LinearProgressIndicator // Import LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold // Import Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar // Import TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // Import stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R // Import R for resources
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType
import com.senlin.budgetmaster.ui.ViewModelFactory // Use ViewModelFactory
import java.text.NumberFormat
import java.time.LocalDate // Import LocalDate
import java.time.format.DateTimeFormatter // Import DateTimeFormatter
import java.time.format.FormatStyle // Optional: For localized date formats
import java.util.Locale // Keep Locale for NumberFormat

@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental TopAppBar
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory.Factory) // Use correct factory
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold( // Use Scaffold to add TopAppBar
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.dashboard_title)) }) // Use dashboard_title string
        }
    ) { innerPadding -> // Content lambda receives padding
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), // Apply innerPadding from Scaffold
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
                // Pass modifier down if needed, but padding is handled by Surface now
                DashboardContent(uiState = uiState)
            }
        }
        // Removed duplicated error/content blocks here
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
            SectionTitle(R.string.dashboard_recent_transactions)
        }
        if (uiState.recentTransactions.isEmpty()) {
            item { Text(stringResource(id = R.string.dashboard_no_recent_transactions)) }
        } else {
            // Wrap items in Cards instead of using dividers
            items(uiState.recentTransactions) { transaction ->
                TransactionItemCard(transaction = transaction)
                // Remove HorizontalDivider
            }
        }


        item {
            SectionTitle(R.string.dashboard_saving_goals)
        }
        if (uiState.goals.isEmpty()) {
            item { Text(stringResource(id = R.string.dashboard_no_saving_goals)) }
        } else {
            // Wrap items in Cards instead of using dividers
            items(uiState.goals) { goal ->
                GoalItemCard(goal = goal)
                // Remove HorizontalDivider
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
                text = stringResource(id = R.string.dashboard_current_balance),
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
fun SectionTitle(titleResId: Int, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(id = titleResId),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp) // Add some vertical padding
    )
}

@Composable
fun TransactionItemCard(transaction: Transaction, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp), // Adjust padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Add Icon based on type
            val icon = if (transaction.type == TransactionType.INCOME) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward
            val iconColor = if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Icon(
                imageVector = icon,
                contentDescription = transaction.type.name,
                tint = iconColor,
                modifier = Modifier.size(24.dp).padding(end = 12.dp) // Add padding to icon
            )

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) { // Take available space
                Text(
                    text = transaction.note ?: transaction.type.name, // Use note or type name
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Amount
            Text(
                text = formatCurrency(transaction.amount),
                style = MaterialTheme.typography.bodyLarge,
                color = iconColor // Use the same color as the icon
            )
        }
    }
}

@Composable
fun GoalItemCard(goal: Goal, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column( // Use Column for Goal Name and Progress
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp) // Adjust padding
        ) {
            Row( // Row for Goal Name and Amount Text
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${formatCurrency(goal.currentAmount)} / ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use a less prominent color
                )
            }
            Spacer(modifier = Modifier.height(8.dp)) // Add space before progress bar

            // Progress Indicator
            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) }, // Ensure progress is between 0 and 1
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- Helper Functions ---

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance() // Use default locale currency
    // You might want to customize the locale: NumberFormat.getCurrencyInstance(Locale("es", "CO")) for COP
    return format.format(amount)
}

fun formatDate(date: LocalDate): String {
    // Use DateTimeFormatter for LocalDate
    // You can choose different styles like FormatStyle.MEDIUM, FormatStyle.LONG, etc.
    // Or define a custom pattern: DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    return date.format(formatter)
}
