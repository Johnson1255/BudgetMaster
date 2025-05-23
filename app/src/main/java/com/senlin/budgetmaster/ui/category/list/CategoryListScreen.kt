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
import androidx.compose.runtime.LaunchedEffect // Import LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.ui.ViewModelFactory
import com.senlin.budgetmaster.ui.theme.BudgetMasterTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    modifier: Modifier = Modifier,
    viewModel: CategoryListViewModel = viewModel(factory = ViewModelFactory.Factory),
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Long) -> Unit, // Changed to Long
    userId: Long? // Add userId parameter
) {
    val uiState by viewModel.uiState.collectAsState()

    // Set the userId in the ViewModel when it's available or changes
    LaunchedEffect(userId) {
        viewModel.setCurrentUserId(userId)
    }

    // The Scaffold is now handled by MainActivity, so we directly use the content
    // and pass the main modifier which might include padding from the main Scaffold.
    Box(modifier = modifier.fillMaxSize()) { // Use a Box to contain FAB and content
        CategoryListContent(
            // Pass the modifier directly, it should include padding from the parent Scaffold in MainActivity
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onEditClick = onEditCategoryClick,
            onDeleteClick = { category -> viewModel.deleteCategory(category) }
        )
        FloatingActionButton(
            onClick = onAddCategoryClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Standard FAB padding
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_category_cd)) // Use string resource
        }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Slightly increased elevation
    ) {
        Row(
            modifier = Modifier
                .padding(all = 16.dp) // Increased padding for more breathing room
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = getLocalizedCategoryName(englishCategoryName = category.name),
                style = MaterialTheme.typography.titleMedium, // Slightly larger text for emphasis
                modifier = Modifier.weight(1f) // Allow text to take available space
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Add space between icons
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.edit_category_cd), // Use string resource
                        tint = MaterialTheme.colorScheme.primary // Consistent icon color
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.delete_category_cd), // Use string resource
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun getLocalizedCategoryName(englishCategoryName: String): String {
    val resourceId = when (englishCategoryName) {
        "Housing" -> R.string.category_housing
        "Transportation" -> R.string.category_transportation
        "Food" -> R.string.category_food
        "Utilities" -> R.string.category_utilities
        "Healthcare" -> R.string.category_healthcare
        "Personal Care" -> R.string.category_personal_care
        "Entertainment" -> R.string.category_entertainment
        "Debt Payments" -> R.string.category_debt_payments
        "Investments" -> R.string.category_investments
        "Miscellaneous/Other" -> R.string.category_miscellaneous_other
        else -> 0 // Fallback: if 0, we'll use the original name from DB
    }
    return if (resourceId != 0) stringResource(id = resourceId) else englishCategoryName
}

@Preview(showBackground = true)
@Composable
fun CategoryItemPreview() {
    BudgetMasterTheme {
        CategoryItem(
            category = Category(id = 1, userId = 1, name = "Food"),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryItemMiscellaneousPreview() {
    BudgetMasterTheme {
        CategoryItem(
            category = Category(id = 1, userId = 1, name = "Miscellaneous/Other"),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryItemUserCreatedPreview() {
    BudgetMasterTheme {
        CategoryItem(
            category = Category(id = 1, userId = 1, name = "My Custom Stuff"),
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
