package com.senlin.budgetmaster.ui.goal.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.navigation.Screen
import com.senlin.budgetmaster.ui.ViewModelFactory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    navController: NavController,
    viewModel: GoalListViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val uiState by viewModel.goalListUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Metas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.GoalEdit.createRoute(0L)) }) { // Use createRoute for new goal (ID 0)
                Icon(Icons.Filled.Add, contentDescription = "Agregar Meta")
            }
        }
    ) { paddingValues ->
        GoalListContent(
            modifier = Modifier.padding(paddingValues),
            goals = uiState.goalList,
            onEditClick = { goalId ->
                navController.navigate(Screen.GoalEdit.createRoute(goalId)) // Use createRoute for editing existing goal
            },
            onDeleteClick = { goal ->
                viewModel.deleteGoal(goal)
            }
        )
    }
}

@Composable
fun GoalListContent(
    modifier: Modifier = Modifier,
    goals: List<Goal>,
    onEditClick: (Long) -> Unit,
    onDeleteClick: (Goal) -> Unit
) {
    if (goals.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay metas aún. ¡Agrega una!")
        }
    } else {
        LazyColumn(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(goals, key = { it.id }) { goal ->
                GoalItem(
                    goal = goal,
                    onEditClick = { onEditClick(goal.id) },
                    onDeleteClick = { onDeleteClick(goal) }
                )
            }
        }
    }
}

@Composable
fun GoalItem(
    goal: Goal,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CO")) // Example: Colombian Peso

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(goal.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress }, // Use lambda overload
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${currencyFormat.format(goal.currentAmount)} / ${currencyFormat.format(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
             // Optional: Display target date if needed
            // goal.targetDate?.let { date ->
            //     Spacer(modifier = Modifier.height(4.dp))
            //     Text("Fecha Límite: ${DateFormat.getDateInstance().format(date)}", style = MaterialTheme.typography.bodySmall)
            // }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar Meta")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar Meta")
                }
            }
        }
    }
}
