package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn
import androidx.compose.foundation.lazy.items // Import items extension
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share // Import Share icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart // Import lineChart
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
    val context = LocalContext.current // Get context for sharing

    // Effect to launch share sheet when CSV content is ready
    LaunchedEffect(uiState.csvContentToShare) {
        uiState.csvContentToShare?.let { csvContent ->
            shareReport(context, csvContent)
            viewModel.clearCsvExportTrigger() // Reset the trigger
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") }, // More general title
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = { // Add actions to TopAppBar
                    IconButton(onClick = { viewModel.prepareCsvExport() }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Export Report"
                        )
                    }
                }
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
                        text = {
                            Text(
                                when (reportType) {
                                    ReportType.EXPENSE_BY_CATEGORY -> "By Category"
                                    ReportType.INCOME_VS_EXPENSE -> "Income/Expense"
                                    ReportType.SPENDING_TREND -> "Trend"
                                }
                            )
                        }
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
    // Producer for Category Expense Chart
    val categoryChartEntryModelProducer = remember { ChartEntryModelProducer() }
    // Producer for Spending Trend Chart
    val trendChartEntryModelProducer = remember { ChartEntryModelProducer() }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy") } // For date range display
    val trendAxisDateFormatter = remember { DateTimeFormatter.ofPattern("M/d") } // Short format for axis

    // State for Date Range Picker Dialog
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = uiState.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        initialSelectedEndDateMillis = uiState.endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    // Update Category chart producer when category data changes
    LaunchedEffect(uiState.categoryExpenses) {
        val entries = uiState.categoryExpenses.mapIndexed { index, expense ->
            entryOf(index.toFloat(), expense.totalAmount.toFloat())
        }
        categoryChartEntryModelProducer.setEntries(entries) { /* optional callback */ }
    }

    // Update Trend chart producer when daily data changes
    LaunchedEffect(uiState.dailyExpenses) {
        val entries = uiState.dailyExpenses.mapIndexed { index, dailyExpense ->
            // Use index for x-axis, actual amount for y-axis
            entryOf(index.toFloat(), dailyExpense.totalAmount.toFloat())
        }
        trendChartEntryModelProducer.setEntries(entries) { /* optional callback */ }
    }


    // --- Define Axis formatters ---
    // For Category Chart (Bottom: Category Name)
    val categoryBottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        uiState.categoryExpenses.getOrNull(value.toInt())?.categoryName ?: ""
    }
    // For Trend Chart (Bottom: Date)
    val trendBottomAxisFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        // Map the index back to the date from the dailyExpenses list
        uiState.dailyExpenses.getOrNull(value.toInt())?.date?.format(trendAxisDateFormatter) ?: ""
    }
    // For Both Charts (Start: Currency)
    val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        currencyFormat.format(value)
    }
    // Removed duplicate definition

    // --- Define Chart components ---
    // For Category Chart
    val categoryColumnChart = columnChart()
    val categoryStartAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter)
    val categoryBottomAxis = rememberBottomAxis(valueFormatter = categoryBottomAxisFormatter)
    // For Trend Chart
    val trendLineChart = lineChart()
    val trendStartAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter)
    // Corrected: Removed 'guidelike' parameter, configure guideline separately if needed
    val trendBottomAxis = rememberBottomAxis(valueFormatter = trendBottomAxisFormatter)


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
                text = "${uiState.startDate.format(displayDateFormatter)} - ${uiState.endDate.format(displayDateFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select Date Range")
            }
        }

        // Predefined Date Range Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // Or Arrangement.spacedBy(4.dp)
        ) {
            val today = LocalDate.now()
            TextButton(onClick = {
                val start = today.withDayOfMonth(1)
                val end = today.withDayOfMonth(today.lengthOfMonth())
                onDateRangeSelected(start, end)
            }) { Text("Month") } // Short label

            TextButton(onClick = {
                val start = today.minusMonths(1).withDayOfMonth(1)
                val end = start.withDayOfMonth(start.lengthOfMonth())
                onDateRangeSelected(start, end)
            }) { Text("Last M.") } // Short label

            TextButton(onClick = {
                val start = today.withDayOfYear(1)
                val end = today // YTD ends today
                onDateRangeSelected(start, end)
            }) { Text("YTD") }
        }


        // Content Area based on selected report type (Chart or Summary Text)
        Box(
            // Removed weight(1f) to allow space for the table below
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                .heightIn(min = 250.dp), // Ensure chart has minimum height
            contentAlignment = Alignment.Center
        ) {
            // Show chart or summary text based on the selected report type
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
                                chart = categoryColumnChart,
                                chartModelProducer = categoryChartEntryModelProducer,
                                startAxis = categoryStartAxis,
                                bottomAxis = categoryBottomAxis,
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
                ReportType.SPENDING_TREND -> {
                    // Placeholder for Spending Trend Chart
                    when {
                        uiState.isLoading -> CircularProgressIndicator()
                        uiState.error != null -> {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        uiState.dailyExpenses.isEmpty() && !uiState.isLoading -> {
                             Text(
                                 text = "No spending data available for the selected period.",
                                 style = MaterialTheme.typography.bodyLarge,
                                 textAlign = TextAlign.Center
                             )
                        }
                        !uiState.isLoading -> {
                            Chart(
                                chart = trendLineChart,
                                chartModelProducer = trendChartEntryModelProducer,
                                startAxis = trendStartAxis,
                                bottomAxis = trendBottomAxis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 250.dp)
                            )
                        }
                    }
                }
                // Add else branch to satisfy exhaustive check, though it shouldn't be reached
                else -> {
                    Text("Unknown report type selected.")
                }
            }
        } // End of Box for Chart/Summary

        // Divider
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Data Table Area (Scrollable)
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                // Optional: Add weight if you want it to take remaining space,
                // but LazyColumn handles scrolling internally.
                // .weight(1f)
        ) {
            // Add headers and items based on report type
            when (uiState.selectedReportType) {
                ReportType.EXPENSE_BY_CATEGORY -> {
                    if (uiState.categoryExpenses.isNotEmpty() && !uiState.isLoading) {
                        item { // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Category", style = MaterialTheme.typography.titleSmall)
                                Text("Amount", style = MaterialTheme.typography.titleSmall)
                            }
                            Divider()
                        }
                        items(uiState.categoryExpenses) { expense ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(expense.categoryName, style = MaterialTheme.typography.bodyMedium)
                                Text(currencyFormat.format(expense.totalAmount), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                ReportType.INCOME_VS_EXPENSE -> {
                     if (!uiState.isLoading) {
                         item { // Header
                             Text("Details", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp))
                             Divider()
                         }
                         item { DataTableRow("Total Income", uiState.totalIncome, currencyFormat, MaterialTheme.colorScheme.primary) }
                         item { DataTableRow("Total Expense", uiState.totalExpense, currencyFormat, MaterialTheme.colorScheme.error) }
                         item { DataTableRow("Net ${if (uiState.totalIncome >= uiState.totalExpense) "Income" else "Loss"}", uiState.totalIncome - uiState.totalExpense, currencyFormat) }
                     }
                }
                ReportType.SPENDING_TREND -> {
                    if (uiState.dailyExpenses.isNotEmpty() && !uiState.isLoading) {
                        item { // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Date", style = MaterialTheme.typography.titleSmall)
                                Text("Amount", style = MaterialTheme.typography.titleSmall)
                            }
                            Divider()
                        }
                        items(uiState.dailyExpenses) { daily ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(daily.date.format(displayDateFormatter), style = MaterialTheme.typography.bodyMedium)
                                Text(currencyFormat.format(daily.totalAmount), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } // End of LazyColumn for Table

    } // End of Main Column

    // Date Range Picker Dialog (Keep outside the main content column)
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

// Helper composable for Income/Expense table rows
@Composable
private fun DataTableRow(label: String, amount: Double, formatter: NumberFormat, amountColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(formatter.format(amount), style = MaterialTheme.typography.bodyMedium, color = amountColor)
    }
}

// Helper function to create and launch the share intent
private fun shareReport(context: Context, reportContent: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, reportContent)
        type = "text/csv" // Set MIME type for CSV
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Report via")
    // Check if there's an app to handle the intent before launching
    if (sendIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(shareIntent)
    } else {
        // Optionally show a toast or message if no app can handle the share action
        // Toast.makeText(context, "No app found to share the report", Toast.LENGTH_SHORT).show()
    }
}
