package com.senlin.budgetmaster.ui.goal.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.senlin.budgetmaster.R // Assuming you have string resources
import com.senlin.budgetmaster.ui.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    navController: NavController,
    viewModel: GoalEditViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val goalUiState = viewModel.goalUiState
    val title = if (goalUiState.id == 0L) "Agregar Meta" else "Editar Meta" // Dynamic title

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        GoalInputForm(
            goalUiState = goalUiState,
            onValueChange = viewModel::updateUiState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Save Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.saveGoal()
                        navController.navigateUp() // Navigate back after saving
                    }
                },
                enabled = goalUiState.isEntryValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Meta")
            }
        }
    }
}

@Composable
fun GoalInputForm(
    goalUiState: GoalUiState,
    onValueChange: (GoalUiState) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit // To place the button
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = goalUiState.name,
            onValueChange = { onValueChange(goalUiState.copy(name = it)) },
            label = { Text("Nombre de la Meta") }, // Replace with stringResource(R.string.goal_name_label)
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = goalUiState.targetAmount,
            onValueChange = { onValueChange(goalUiState.copy(targetAmount = it)) },
            label = { Text("Monto Objetivo") }, // Replace with stringResource(R.string.goal_target_amount_label)
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        // Add fields for currentAmount and targetDate if needed
        // Example for currentAmount (might not be editable directly here)
        /*
        OutlinedTextField(
            value = goalUiState.currentAmount,
            onValueChange = { onValueChange(goalUiState.copy(currentAmount = it)) },
            label = { Text("Monto Actual (Opcional)") }, // Replace with stringResource
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberDecimal),
            singleLine = true,
            enabled = false // Usually not directly editable here
        )
        */

        // Placeholder for Date Picker for targetDate
        // Button(onClick = { /* TODO: Show Date Picker */ }) { Text("Seleccionar Fecha Límite (Opcional)") }
        // goalUiState.targetDate?.let { Text("Fecha seleccionada: ${DateFormat.getDateInstance().format(it)}") }

        content() // Render the button passed from the parent
    }
}
