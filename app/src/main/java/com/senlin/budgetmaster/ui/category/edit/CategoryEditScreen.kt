package com.senlin.budgetmaster.ui.category.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Corrected import
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.ui.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryEditViewModel = viewModel(factory = ViewModelFactory.Factory),
    navigateBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState = viewModel.categoryUiState
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveComplete()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            // Consider resetting the error in the ViewModel after showing it
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == 0L) "Add Category" else "Edit Category") }, // Use 0L for Long comparison
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.saveCategory()
                                // Navigation happens via LaunchedEffect on uiState.isSaved
                            }
                        },
                        enabled = uiState.isEntryValid && !uiState.isLoading
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Category")
                    }
                }
            )
        }
    ) { paddingValues ->
        CategoryEditContent(
            modifier = modifier.padding(paddingValues),
            uiState = uiState,
            onNameChange = { viewModel.updateUiState(it) }
        )
    }
}

@Composable
fun CategoryEditContent(
    modifier: Modifier = Modifier,
    uiState: CategoryUiState,
    onNameChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.isLoading && uiState.id != 0L) { // Use 0L for Long comparison
             Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = !uiState.isEntryValid && uiState.name.isNotEmpty() // Show error if invalid after typing
            )
            if (uiState.isLoading && uiState.id == 0L) { // Use 0L for Long comparison
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
