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
// Add AlertDialog and TextButton imports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Import TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton // Import IconButton
import androidx.compose.material3.MenuAnchorType // Added import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import Back Arrow Icon
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState // Import rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.senlin.budgetmaster.R
import com.senlin.budgetmaster.data.model.Category
import com.senlin.budgetmaster.data.model.Goal
import com.senlin.budgetmaster.data.model.TransactionType
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
    val currentNavigateBack by rememberUpdatedState(navigateBack) // Ensure latest lambda

    // --- Listen for Save Success ---
    LaunchedEffect(viewModel) { // Key effect to viewModel instance
        viewModel.saveSuccessEvent.collect {
            currentNavigateBack() // Use the updated state lambda
        }
    }

    // --- Show Error Dialog ---
    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissErrorDialog() }, // Dismiss if clicked outside
            title = { Text("Error") }, // Or a more specific title like "Validation Error"
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissErrorDialog() }) {
                    Text("OK")
                }
            }
        )
    }
    // --- End Error Dialog ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.transactionId == null) stringResource(R.string.add_transaction_title) else stringResource(R.string.edit_transaction_title)) }, // Use string resources
                navigationIcon = {
                    IconButton(onClick = currentNavigateBack) { // Use updated state lambda
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_description) // Add content description
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TransactionEditForm(
                uiState = uiState, // Ensure only one uiState argument persists
                onAmountChange = viewModel::updateAmount,
                onTypeChange = viewModel::updateType,
                onSelectedItemChange = viewModel::updateSelectedItem, // Use combined callback
                onDateChange = viewModel::updateDate,
                onNoteChange = viewModel::updateNote,
                onSaveClick = {
                    viewModel.saveTransaction() // Only call save here
                    // Navigation is handled by LaunchedEffect listening to saveSuccessEvent
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable // Ensure only one @Composable annotation persists
fun TransactionEditForm(
    uiState: TransactionEditUiState,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onSelectedItemChange: (Any?) -> Unit, // Combined callback
    onDateChange: (LocalDate) -> Unit,
    onNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
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
            label = { Text(stringResource(R.string.amount_label)) }, // Use stringResource
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        // Transaction Type Radio Buttons
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.type_label), style = MaterialTheme.typography.bodyLarge) // Use stringResource
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

        // Combined Category/Goal Selector
        CombinedSelector(
            selectedItem = uiState.selectedItem,
            items = uiState.availableItems,
            onItemSelected = onSelectedItemChange,
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
            label = { Text(stringResource(R.string.note_label_optional)) }, // Use stringResource
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Save Button
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isSaving && uiState.selectedItem != null && uiState.amount.isNotBlank(), // Use selectedItem for validation
            modifier = Modifier.align(Alignment.End)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp)) // M3 CircularProgressIndicator, color is handled by theme
            } else {
                Text(stringResource(R.string.save_button)) // Use stringResource
            }
        }
    }
}

// Removed CategorySelector and GoalSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedSelector(
    selectedItem: Any?, // Can be Category or Goal
    items: List<Any>, // Combined list of Categories and Goals
    onItemSelected: (Any?) -> Unit, // Callback for selection
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Determine the display name based on the type of the selected item
    val selectedItemText = when (selectedItem) {
        is Category -> selectedItem.name
        is Goal -> selectedItem.name + " (Goal)" // Indicate it's a goal
        else -> "Select Category or Goal" // Placeholder text
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedItemText,
            onValueChange = { }, // Read-only
            readOnly = true,
            label = { Text(stringResource(R.string.category_goal_label)) }, // Use stringResource
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true) // Add menuAnchor modifier for M3
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Add a "None" or default option if needed, depending on requirements
            // DropdownMenuItem(
            //     onClick = {
            //         onItemSelected(null) // Allow deselecting?
            //         expanded = false
            //     },
            //     text = { Text("None") }
            // )

            items.forEach { item ->
                val itemText = when (item) {
                    is Category -> item.name
                    is Goal -> item.name + " (Goal)" // Indicate goal in the list
                    else -> "Unknown Item" // Should not happen
                }
                DropdownMenuItem(
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    text = { Text(itemText) }
                )
            }
            if (items.isEmpty()) {
                DropdownMenuItem(
                    onClick = { expanded = false },
                    enabled = false,
                    text = { Text(stringResource(R.string.no_categories_goals_available)) } // Use stringResource
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
        label = { Text(stringResource(R.string.date_label)) }, // Use stringResource
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.select_date_description), // Use stringResource
                modifier = Modifier.clickable { datePickerDialog.show() }
            )
        },
        modifier = modifier.clickable { datePickerDialog.show() } // Make the whole field clickable
    )
}
