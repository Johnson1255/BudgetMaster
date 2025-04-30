package com.senlin.budgetmaster.ui.transaction.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType // Import TransactionType from model
import com.senlin.budgetmaster.navigation.Screen
import com.senlin.budgetmaster.ui.ViewModelFactory // Use ViewModelFactory
import java.time.format.DateTimeFormatter // Use DateTimeFormatter for LocalDate
import java.time.format.FormatStyle // For localized date formats
import java.util.* // Keep Locale for formatter

@Composable
fun TransactionListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = viewModel(factory = ViewModelFactory.Factory) // Use correct factory
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigate to Add/Edit screen for adding a new transaction (-1 indicates new)
                    navController.navigate(Screen.AddEditTransaction.createRoute(null))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Transaction", tint = Color.White)
            }
        },
        modifier = modifier
    ) { paddingValues ->
        TransactionListContent(
            transactions = uiState.transactions,
            categoryMap = uiState.categoryMap, // Pass the map
            isLoading = uiState.isLoading,
            onTransactionClick = { transactionId ->
                // Navigate to Add/Edit screen for editing the selected transaction
                navController.navigate(Screen.AddEditTransaction.createRoute(transactionId))
            },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun TransactionListContent(
    transactions: List<Transaction>,
    categoryMap: Map<Long, String>, // Accept the map
    isLoading: Boolean,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Transactions", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet. Tap '+' to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        categoryMap = categoryMap, // Pass the map down
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    categoryMap: Map<Long, String>, // Accept the map
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Look up category name from the map, provide a default if not found
    val categoryName = categoryMap[transaction.categoryId] ?: "Unknown Category"
    // Use type property from Transaction model
    val amountColor = if (transaction.type == TransactionType.INCOME) Color(0xFF008000) else Color.Red // Dark Green for income, Red for expense
    val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
    // Use DateTimeFormatter for LocalDate
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    val formattedDate = transaction.date.format(formatter)

    Card( // Use M3 Card
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Use M3 CardDefaults
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp) // Use dp import
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(categoryName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold) // Use M3 Text
                if (!transaction.note.isNullOrBlank()) {
                    Text(transaction.note, style = MaterialTheme.typography.bodySmall, color = Color.Gray) // Use M3 Text
                }
                Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray) // Use M3 Text
            }
            Text( // Use M3 Text
                text = "$sign${"%.2f".format(transaction.amount)}", // Format to 2 decimal places
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                modifier = Modifier.padding(start = 16.dp) // Use dp import
            )
        }
    }
}
