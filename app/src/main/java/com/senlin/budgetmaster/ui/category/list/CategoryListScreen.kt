package com.senlin.budgetmaster.ui.category.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.ui.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryListViewModel = viewModel(factory = ViewModelFactory.Factory),
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Long) -> Unit // Changed to Long
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = R.string.categories_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategoryClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        }
    ) { paddingValues ->
        CategoryListContent(
            modifier = Modifier.padding(paddingValues), // Pass modifier with padding
            uiState = uiState,
            onEditClick = onEditCategoryClick,
            onDeleteClick = { category -> viewModel.deleteCategory(category) }
        )
    }
}

@Composable
fun CategoryListContent(
    modifier: Modifier = Modifier,
    uiState: CategoryListUiState,
    onEditClick: (Long) -> Unit, // Changed to Long
    onDeleteClick: (Category) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (uiState.categories.isEmpty()) {
            Text(
                text = "No categories found. Add one!",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onEditClick = { onEditClick(category.id) },
                        onDeleteClick = { onDeleteClick(category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge)
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Category")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
