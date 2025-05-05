package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.senlin.budgetmaster.ui.ViewModelFactory
import com.senlin.budgetmaster.ui.report.ReportType // Import ReportType
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val reportTypes = ReportType.values() // Get all report types

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") }, // More general title
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) { // Wrap content in a Column
            // Tab Row for Report Type Selection
            TabRow(
                selectedTabIndex = reportTypes.indexOf(uiState.selectedReportType),
                containerColor = MaterialTheme.colorScheme.surface, // Match background
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                reportTypes.forEach { reportType ->
                    Tab(
                        selected = uiState.selectedReportType == reportType,
                        onClick = { viewModel.updateSelectedReportType(reportType) },
                        text = { Text(reportType.name.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() }) } // Format enum name
                    )
                }
            }

            // Report Content Area
            ReportContent(
                uiState = uiState,
                onDateRangeSelected = { start, end -> viewModel.updateDateRange(start, end) }, // Pass lambda
                modifier = Modifier.fillMaxSize() // Content fills remaining space
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Needed for DateRangePicker
@Composable
fun ReportContent(
    uiState: ReportUiState,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit, // Callback for date selection
    modifier: Modifier = Modifier,
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) }
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") } // Formatter for display

    // State for Date Range Picker Dialog
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        initialSelectedEndDateMillis = uiState.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Update chart producer when data changes
    LaunchedEffect(uiState.categoryExpenses) {
        val entries = uiState.categoryExpenses.mapIndexed { index, expense ->
            entryOf(index.toFloat(), expense.totalAmount.toFloat())
        }
        chartEntryModelProducer.setEntries(entries) { /* optional callback */ }
    }

    // Define Axis formatters
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        uiState.categoryExpenses.getOrNull(value.toInt())?.categoryName ?: ""
    }
    val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        currencyFormat.format(value)
    }

    // Define Chart components
    val columnChart = columnChart()
    val startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter)
    val bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter)

    Column( // Use Column to stack elements vertically
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date Range Display and Selection Button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${uiState.startDate.format(dateFormatter)} - ${uiState.endDate.format(dateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date Range")
            }
        }

        // Content Area based on selected report type
        Box( // Keep the Box for consistent alignment and weighting
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp), // Add some padding above content
            contentAlignment = Alignment.Center
        ) {
            // Show content based on the selected report type
            when (uiState.selectedReportType) {
                ReportType.EXPENSE_BY_CATEGORY -> {
                    // Existing Chart Logic for Expenses by Category
                    when {
                        uiState.isLoading -> CircularProgressIndicator()
                        uiState.error != null -> {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        uiState.categoryExpenses.isEmpty() && !uiState.isLoading -> {
                            Text(
                                text = "No expense data available for the selected period.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Chart(
                                chart = columnChart,
                                chartModelProducer = chartEntryModelProducer,
                                startAxis = startAxis,
                                bottomAxis = bottomAxis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 250.dp)
                            )
                        }
                    }
                }
                ReportType.INCOME_VS_EXPENSE -> {
                    // New Content for Income vs. Expense Summary
                    when {
                        uiState.isLoading -> CircularProgressIndicator()
                        uiState.error != null -> {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        // Show summary even if income/expense is zero, unless loading/error
                        !uiState.isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize() // Center content in the box
                            ) {
                                Text("Summary for Period", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly // Space out items
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Income", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = currencyFormat.format(uiState.totalIncome),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.primary // Use theme colors
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Total Expense", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = currencyFormat.format(uiState.totalExpense),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.error // Use theme colors
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                // Optional: Add Net Income/Loss calculation
                                val netAmount = uiState.totalIncome - uiState.totalExpense
                                Text(
                                    "Net ${if (netAmount >= 0) "Income" else "Loss"}: ${currencyFormat.format(netAmount)}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
                // Add cases for other report types later
            }
        }
    }

    // Date Range Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // Get selected dates (handle potential nulls)
                        val selectedStartMillis = datePickerState.selectedStartDateMillis
                        val selectedEndMillis = datePickerState.selectedEndDateMillis

                        if (selectedStartMillis != null && selectedEndMillis != null) {
                            val newStartDate = Instant.ofEpochMilli(selectedStartMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val newEndDate = Instant.ofEpochMilli(selectedEndMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateRangeSelected(newStartDate, newEndDate) // Call the callback
                        }
                    },
                    enabled = datePickerState.selectedStartDateMillis != null && datePickerState.selectedEndDateMillis != null // Enable only when range selected
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { // Content of the dialog
            DateRangePicker(state = datePickerState, modifier = Modifier.padding(16.dp))
        }
    }
}
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                uiState.categoryExpenses.isEmpty() && !uiState.isLoading -> { // Check isLoading false
                    Text(
                        text = "No expense data available for the selected period.", // Updated message
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Chart(
                        chart = columnChart,
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = startAxis,
                        bottomAxis = bottomAxis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp) // Use heightIn for flexibility
                    )
                }
            }
        }
    }

    // Date Range Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                        // Get selected dates (handle potential nulls)
                        val selectedStartMillis = datePickerState.selectedStartDateMillis
                        val selectedEndMillis = datePickerState.selectedEndDateMillis

                        if (selectedStartMillis != null && selectedEndMillis != null) {
                            val newStartDate = Instant.ofEpochMilli(selectedStartMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            val newEndDate = Instant.ofEpochMilli(selectedEndMillis)
                                .atZone(ZoneId.systemDefault()).toLocalDate()
                            onDateRangeSelected(newStartDate, newEndDate) // Call the callback
                        }
                    },
                    enabled = datePickerState.selectedStartDateMillis != null && datePickerState.selectedEndDateMillis != null // Enable only when range selected
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) { // Content of the dialog
            DateRangePicker(state = datePickerState, modifier = Modifier.padding(16.dp))
        }
    }
}
