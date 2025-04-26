package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
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
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.senlin.budgetmaster.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.*

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
        if (uiState.categoryExpenses.isNotEmpty()) {
            chartEntryModelProducer.setEntries(
                uiState.categoryExpenses.mapIndexed { index, expense ->
                    entryOf(index.toFloat(), expense.totalAmount.toFloat())
                }
            )
        } else {
            chartEntryModelProducer.setEntries(emptyList()) // Clear entries if data is empty
        }
    }

    // Define Axis formatters
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        uiState.categoryExpenses.getOrNull(value.toInt())?.categoryName ?: ""
    }
    val startAxisValueFormatter = AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
        currencyFormat.format(value)
    }

    // Define Chart components (using remember for performance)
    val columnChart = remember {
        columnChart(
            // Optional: Customize columns, e.g., using dynamic colors based on data
            // columns = uiState.categoryExpenses.map { expense ->
            //     LineComponent(
            //         color = Color(expense.color).toArgb(), // Use category color
            //         thicknessDp = 8f, // Adjust thickness
            //         shape = Shapes.roundedCornerShape(allPercent = 40),
            //     )
            // }
        )
    }
    val startAxis = rememberStartAxis(valueFormatter = startAxisValueFormatter)
    val bottomAxis = rememberBottomAxis(valueFormatter = bottomAxisValueFormatter)


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

// Remove the old Legend composable if it exists
/*
@Composable
fun Legend(slices: List<PieChartData.Slice>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(modifier = Modifier.size(16.dp).background(slice.color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = slice.label ?: "Unknown") // Use label if available
            }
        }
    }
}

// Optional: Simple Legend Composable (if needed)
/*
@Composable
fun Legend(slices: List<PieChartData.Slice>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                Box(modifier = Modifier.size(16.dp).background(slice.color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = slice.label ?: "Unknown") // Use label if available
            }
        }
    }
}
*/
