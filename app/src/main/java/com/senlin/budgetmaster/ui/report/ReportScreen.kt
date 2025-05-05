package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.* // Use wildcard import for runtime
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Report") }, // Simplified title
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        ReportContent(
            uiState = uiState,
            onDateRangeSelected = { start, end -> viewModel.updateDateRange(start, end) }, // Pass lambda
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        )
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

        // Chart Area Box
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f), // Allow chart to take remaining space
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
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
