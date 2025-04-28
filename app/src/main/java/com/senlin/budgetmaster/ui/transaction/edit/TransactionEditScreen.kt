package com.senlin.budgetmaster.ui.transaction.edit

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
// Remove wildcard import for material 2
// import androidx.compose.material.*
// Add specific Material 3 imports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api // Use M3 experimental API
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R // Assuming R class is generated correctly
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.TransactionType // Import TransactionType
import com.senlin.budgetmaster.ui.ViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn at file level or ensure it covers all usages
@Composable
fun TransactionEditScreen(
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionEditViewModel = viewModel(factory = ViewModelFactory.Factory) // Use your factory
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.transactionId == null) "Add Transaction" else "Edit Transaction") }, // Adjust title based on mode
                // Add navigation icon if needed
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.isError) {
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Error loading data. Please try again.") // Simple error message
            }
        }
         else {
            TransactionEditForm(
                uiState = uiState,
                onAmountChange = viewModel::updateAmount,
                onTypeChange = viewModel::updateType,
                onCategoryChange = viewModel::updateCategory,
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSaveClick = {
                    viewModel.saveTransaction()
                    // Consider observing a save success state before navigating back
                    navigateBack() // Navigate back after save attempt
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun TransactionEditForm(
    uiState: TransactionEditUiState,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDateChange: (LocalDate) -> Unit, // Ensure this matches ViewModel
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
    // Removed duplicated lines below
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp) // Use dp import
            .verticalScroll(rememberScrollState()), // Make content scrollable
        verticalArrangement = Arrangement.spacedBy(16.dp) // Use dp import
    ) {
        // Amount Field
        OutlinedTextField(
            value = uiState.amount,
            onValueChange = onAmountChange,
            label = { Text("Amount") }, // Replace with stringResource
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Transaction Type Radio Buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Type:", style = MaterialTheme.typography.bodyLarge) // Use M3 Typography & Replace with stringResource
            Spacer(Modifier.width(8.dp)) // Use dp import
            Row {
                TransactionType.values().forEach { type -> // Use imported TransactionType
                    Row(
                        Modifier.clickable { onTypeChange(type) }.padding(horizontal = 8.dp), // Use dp import
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton( // Use M3 RadioButton
                            selected = (type == uiState.type),
                            onClick = { onTypeChange(type) }
                        )
                        Text(
                            text = type.name, // Consider more user-friendly names
                            style = MaterialTheme.typography.bodyLarge, // Use M3 typography
                            modifier = Modifier.padding(start = 4.dp) // Use dp import
                        )
                    }
                }
            }
        }


        // Category Selector (Dropdown)
        CategorySelector(
            selectedCategory = uiState.selectedCategory,
            categories = uiState.availableCategories,
            onCategorySelected = onCategoryChange,
            modifier = Modifier.fillMaxWidth()
        )

        // Date Picker
        DatePickerField(
            selectedDate = uiState.date,
            onDateSelected = onDateChange,
            modifier = Modifier.fillMaxWidth()
        )

        // Note Field
        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            label = { Text("Note (Optional)") }, // Replace with stringResource
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Save Button
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving && uiState.selectedCategory != null && uiState.amount.isNotBlank(), // Basic validation
            modifier = Modifier.align(Alignment.End)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // M3 CircularProgressIndicator, color is handled by theme
            } else {
                Text("Save") // Replace with stringResource
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Use M3 Experimental API
@Composable
fun CategorySelector(
    selectedCategory: Category?,
    categories: List<Category>,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "Select Category", // Replace with stringResource
            onValueChange = { }, // Read-only
            readOnly = true,
            label = { Text("Category") }, // Replace with stringResource
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    },
                    text = { Text(category.name) } // Use text lambda parameter
                )
            }
             if (categories.isEmpty()) {
                DropdownMenuItem(
                    onClick = { expanded = false },
                    enabled = false,
                    text = { Text("No categories available") } // Use text lambda parameter & Replace with stringResource
                )
            }
        }
    }
}


@Composable
fun DatePickerField(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val year = selectedDate.year
    val month = selectedDate.monthValue - 1 // Calendar month is 0-indexed
    val day = selectedDate.dayOfMonth

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            onDateSelected(LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth))
        }, year, month, day
    )

    OutlinedTextField(
        value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE), // Or a more user-friendly format
        onValueChange = {}, // Not directly editable
        readOnly = true,
        label = { Text("Date") }, // Replace with stringResource
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Select Date", // Replace with stringResource
                modifier = Modifier.clickable { datePickerDialog.show() }
            )
        },
        modifier = modifier.clickable { datePickerDialog.show() } // Make the whole field clickable
    )
}
