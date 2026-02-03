package com.pecule.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.ui.theme.PeculeTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    isExpense: Boolean,
    isEditing: Boolean,
    label: String,
    amount: Double?,
    category: Category?,
    date: LocalDate,
    isFixed: Boolean,
    errors: List<String>,
    onLabelChange: (String) -> Unit,
    onAmountChange: (Double?) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onIsFixedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var amountText by remember(amount) { mutableStateOf(amount?.toString() ?: "") }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    )

    // Dialog title based on mode
    val dialogTitle = when {
        isEditing && isExpense -> "Modifier dépense"
        isEditing && !isExpense -> "Modifier revenu"
        !isEditing && isExpense -> "Nouvelle dépense"
        else -> "Nouveau revenu"
    }

    // Switch label based on type
    val switchLabel = if (isExpense) "Charge fixe" else "Revenu fixe"

    // Extract error messages for specific fields
    val labelError = errors.find { it.contains("libellé") }
    val amountError = errors.find { it.contains("montant") }
    val categoryError = errors.find { it.contains("catégorie") }
    val dateError = errors.find { it.contains("date") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = dialogTitle,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Label field
                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChange,
                    label = { Text("Libellé") },
                    placeholder = { Text(if (isExpense) "Ex: Courses Carrefour" else "Ex: Prime") },
                    singleLine = true,
                    isError = labelError != null,
                    supportingText = labelError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // Allow only valid decimal input
                        val filtered = newValue.replace(",", ".")
                        if (filtered.isEmpty() || filtered.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amountText = newValue
                            onAmountChange(filtered.toDoubleOrNull())
                        }
                    },
                    label = { Text("Montant") },
                    placeholder = { Text("0.00") },
                    suffix = { Text("€") },
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown (only for expenses)
                if (isExpense) {
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = category?.label ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Catégorie") },
                            placeholder = { Text("Sélectionner") },
                            isError = categoryError != null,
                            supportingText = categoryError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            Category.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.label) },
                                    onClick = {
                                        onCategoryChange(cat)
                                        categoryExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                // Date field
                val dateInteractionSource = remember { MutableInteractionSource() }
                LaunchedEffect(dateInteractionSource) {
                    dateInteractionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
                            showDatePicker = true
                        }
                    }
                }

                OutlinedTextField(
                    value = date.format(dateFormatter),
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    singleLine = true,
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    interactionSource = dateInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )

                // Fixed switch
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = switchLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isFixed,
                        onCheckedChange = onIsFixedChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateChange(newDate)
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Wrapper that connects to AddTransactionViewModel
 */
@Composable
fun AddTransactionDialogWithViewModel(
    viewModel: AddTransactionViewModel,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    val uiState by remember { viewModel.uiState }.let {
        val state = remember { mutableStateOf(AddTransactionUiState()) }
        LaunchedEffect(viewModel) {
            viewModel.uiState.collect { state.value = it }
        }
        state
    }

    AddTransactionDialog(
        isExpense = uiState.isExpense,
        isEditing = uiState.isEditing,
        label = uiState.label,
        amount = uiState.amount,
        category = uiState.category,
        date = uiState.date,
        isFixed = uiState.isFixed,
        errors = errors,
        onLabelChange = { viewModel.updateLabel(it) },
        onAmountChange = { viewModel.updateAmount(it) },
        onCategoryChange = { viewModel.updateCategory(it) },
        onDateChange = { viewModel.updateDate(it) },
        onIsFixedChange = { viewModel.toggleIsFixed() },
        onDismiss = onDismiss,
        onSave = {
            scope.launch {
                val result = viewModel.save()
                if (result.isSuccess) {
                    onSaveSuccess()
                } else {
                    errors = result.errors
                }
            }
        }
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun AddTransactionDialogExpensePreview() {
    PeculeTheme {
        AddTransactionDialog(
            isExpense = true,
            isEditing = false,
            label = "",
            amount = null,
            category = null,
            date = LocalDate.now(),
            isFixed = false,
            errors = emptyList(),
            onLabelChange = {},
            onAmountChange = {},
            onCategoryChange = {},
            onDateChange = {},
            onIsFixedChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionDialogIncomePreview() {
    PeculeTheme {
        AddTransactionDialog(
            isExpense = false,
            isEditing = false,
            label = "Prime",
            amount = 500.0,
            category = null,
            date = LocalDate.of(2025, 1, 25),
            isFixed = false,
            errors = emptyList(),
            onLabelChange = {},
            onAmountChange = {},
            onCategoryChange = {},
            onDateChange = {},
            onIsFixedChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionDialogWithErrorsPreview() {
    PeculeTheme {
        AddTransactionDialog(
            isExpense = true,
            isEditing = false,
            label = "",
            amount = null,
            category = null,
            date = LocalDate.now(),
            isFixed = false,
            errors = listOf(
                "Le libellé est requis",
                "Le montant est requis",
                "La catégorie est requise"
            ),
            onLabelChange = {},
            onAmountChange = {},
            onCategoryChange = {},
            onDateChange = {},
            onIsFixedChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTransactionDialogEditModePreview() {
    PeculeTheme {
        AddTransactionDialog(
            isExpense = true,
            isEditing = true,
            label = "Courses Carrefour",
            amount = 85.50,
            category = Category.FOOD,
            date = LocalDate.of(2025, 1, 20),
            isFixed = false,
            errors = emptyList(),
            onLabelChange = {},
            onAmountChange = {},
            onCategoryChange = {},
            onDateChange = {},
            onIsFixedChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddTransactionDialogDarkPreview() {
    PeculeTheme(darkTheme = true) {
        AddTransactionDialog(
            isExpense = true,
            isEditing = false,
            label = "Restaurant",
            amount = 42.0,
            category = Category.ENTERTAINMENT,
            date = LocalDate.now(),
            isFixed = false,
            errors = emptyList(),
            onLabelChange = {},
            onAmountChange = {},
            onCategoryChange = {},
            onDateChange = {},
            onIsFixedChange = {},
            onDismiss = {},
            onSave = {}
        )
    }
}
