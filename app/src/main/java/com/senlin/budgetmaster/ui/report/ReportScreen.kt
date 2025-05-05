package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect // Add specific import
import androidx.compose.runtime.remember // Add specific import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
// Removed unused Vico imports: verticalGradient, LineComponent, Shapes
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.senlin.budgetmaster.ui.ViewModelFactory
// Removed duplicate imports like Alignment, Modifier, Material components covered by wildcard
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for experimental Material 3 APIs
@Composable
fun ReportScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(factory = ViewModelFactory.Factory) // Use ViewModelFactory
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Report by Category") },
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
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        )
    }
}

@Composable
fun ReportContent(
    uiState: ReportUiState,
    modifier: Modifier = Modifier,
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("es", "CO")) } // Use remember
    val chartEntryModelProducer = remember { ChartEntryModelProducer() } // Vico producer

    // Update producer when data changes
    LaunchedEffect(uiState.categoryExpenses) {
        val entries = uiState.categoryExpenses.mapIndexed { index, expense ->
            entryOf(index.toFloat(), expense.totalAmount.toFloat())
         }
         // Pass the list of entries directly. Vico's setEntries can handle List<ChartEntry>.
         // The producer manages wrapping it if needed internally.
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
    val columnChart = columnChart() // Define outside remember
    val startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter) // Keep axes remembered
    val bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter) // Keep axes remembered


    Box(
        modifier = modifier.padding(16.dp), // Keep padding
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
            uiState.categoryExpenses.isEmpty() -> {
                Text(
                    text = "No expense data available to generate report.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
            else -> {
                 Column(
                     modifier = Modifier.fillMaxSize(), // Fill the available space
                     horizontalAlignment = Alignment.CenterHorizontally
                 ) {
                    Text(
                        "Total Expenses by Category",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Chart(
                        chart = columnChart,
                        chartModelProducer = chartEntryModelProducer,
                        startAxis = startAxis,
                        bottomAxis = bottomAxis,
                        modifier = Modifier
                            .fillMaxWidth() // Take full width
                            .height(300.dp) // Set a fixed height or use weight
                    )
                 }
            }
        }
    }
}

// Removed commented-out Legend composables
