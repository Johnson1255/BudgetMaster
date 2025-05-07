package com.senlin.budgetmaster.ui.transaction.list

import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsBus

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button // Add Button import
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api // Needed for DatePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable // Add rememberSaveable import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource // Import stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.senlin.budgetmaster.R // Import R
import com.senlin.budgetmaster.data.model.Transaction
import com.senlin.budgetmaster.data.model.TransactionType // Import TransactionType from model
import com.senlin.budgetmaster.navigation.Screen
import com.senlin.budgetmaster.ui.ViewModelFactory // Use ViewModelFactory
import java.time.Instant // Add Instant import
import java.time.LocalDate
import java.time.ZoneId // Add ZoneId import
import java.time.format.DateTimeFormatter // Use DateTimeFormatter for LocalDate
import java.time.format.FormatStyle // For localized date formats
import java.util.Locale // Keep Locale for formatter

val categoryIcons = mapOf(
    "Salary" to Icons.Filled.ShoppingCart,
    "Food" to Icons.Filled.Restaurant,
    "Transport" to Icons.Filled.DirectionsBus
)

@Composable
fun TransactionListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TransactionListViewModel = viewModel(factory = ViewModelFactory.Factory) // Use correct factory
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) { // Use Box to contain FAB and content
            TransactionListContent(
                transactions = uiState.transactions,
                categoryMap = uiState.categoryMap, // Pass the map
                isLoading = uiState.isLoading,
                onTransactionClick = { transactionId ->
                    // Navigate to Add/Edit screen for editing the selected transaction
                    navController.navigate(Screen.AddEditTransaction.createRoute(transactionId))
                },
                viewModel = viewModel, // Pass the viewModel down
                // The modifier here should already contain padding from MainActivity's Scaffold
                modifier = Modifier.fillMaxSize()
            )
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddEditTransaction.createRoute(null))
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_transaction_cd), tint = Color.White) // Use string resource
            }
        }
    }
}

// Remove the first, incorrect definition that was duplicated.

@OptIn(ExperimentalMaterial3Api::class) // Opt-in for DatePicker API
@Composable
private fun TransactionListContent(
    transactions: List<Transaction>,
    categoryMap: Map<Long, String>, // Accept the map
    isLoading: Boolean,
    onTransactionClick: (Long) -> Unit,
    viewModel: TransactionListViewModel, // Add viewModel parameter
    modifier: Modifier = Modifier
) {
    // Get dates from ViewModel state
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    // State for showing Date Pickers
    val showStartDatePicker = rememberSaveable { mutableStateOf(false) }
    val showEndDatePicker = rememberSaveable { mutableStateOf(false) }
    val startDateState = rememberDatePickerState()
    val endDateState = rememberDatePickerState(initialSelectedDateMillis = endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())


    // --- Date Picker Dialogs ---
    if (showStartDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showStartDatePicker.value = false
                    startDateState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.setStartDate(selectedDate) // Call ViewModel
                    } ?: viewModel.setStartDate(null) // Handle case where date is cleared in picker
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker.value = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = startDateState)
        }
    }

    if (showEndDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    showEndDatePicker.value = false
                    endDateState.selectedDateMillis?.let { millis ->
                         val selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                         viewModel.setEndDate(selectedDate) // Call ViewModel
                    } ?: viewModel.setEndDate(null) // Handle case where date is cleared in picker
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker.value = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = endDateState)
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp) // Keep internal padding for content
    ) {
        // Title is now handled by AppTopBar in MainActivity

        // --- Date Filter Row ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { showStartDatePicker.value = true }) {
                Text(startDate?.let { formatDate(it) } ?: "Start Date") // Use startDate from ViewModel
            }
            Text("to", modifier = Modifier.padding(horizontal = 8.dp))
            Button(onClick = { showEndDatePicker.value = true }) {
                Text(endDate?.let { formatDate(it) } ?: "End Date") // Use endDate from ViewModel
            }
             // Add Clear Button
            TextButton(onClick = { viewModel.clearDateFilter() }) {
                Text("Clear")
            }
        }

        // --- Transaction List ---
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions recorded yet. Add your first transaction by tapping the '+' button below!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItemCard(
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
private fun TransactionItemCard(
    transaction: Transaction,
    categoryMap: Map<Long, String>, // Accept the map
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = categoryIcons[categoryMap[transaction.categoryId]] ?: Icons.Filled.ShoppingCart,
                contentDescription = categoryMap[transaction.categoryId] ?: "Unknown Category",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                // Display Category Name
                Text(
                    text = categoryMap[transaction.categoryId] ?: "Unknown Category",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                // Display Note (if available)
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Display Formatted Date
                Text(
                    text = formatDate(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Display Amount with Sign and Color
            val amountColor =
                if (transaction.type == TransactionType.INCOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            val sign = if (transaction.type == TransactionType.INCOME) "+" else "-"
            Text(
                text = "$sign${"%.2f".format(transaction.amount)}", // Format to 2 decimal places
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = amountColor,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

// --- Helper Functions ---
fun formatDate(date: LocalDate): String {
    // Use DateTimeFormatter for LocalDate
    // You can choose different styles like FormatStyle.MEDIUM, FormatStyle.LONG, etc.
    // Or define a custom pattern: DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    return date.format(formatter)
}
