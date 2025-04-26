package com.senlin.budgetmaster.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.animation.simpleChartAnimation
import com.senlin.budgetmaster.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

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
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")) // Example: Colombian Peso

    Box(
        modifier = modifier.padding(16.dp),
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
                // Convert CategoryExpense to PieChartData.Slice
                val slices = uiState.categoryExpenses.map { expense ->
                    PieChartData.Slice(
                        value = expense.totalAmount.toFloat(),
                        color = Color(expense.color), // Use color from data
                        label = "${expense.categoryName}: ${currencyFormat.format(expense.totalAmount)}" // Add label
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Expenses by Category", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    PieChart(
                        pieChartData = PieChartData(slices = slices),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp), // Adjust height as needed
                        animation = simpleChartAnimation(),
                        sliceDrawer = com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer(
                            sliceThickness = 80f // Adjust thickness
                        )
                    )
                    // Optional: Add a legend below the chart if needed
                    // Legend(slices = slices, modifier = Modifier.padding(top = 16.dp))
                }
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
