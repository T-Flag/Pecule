package com.pecule.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSalaryDialog(
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, date: LocalDate) -> Unit
) {
    var salaryAmount by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val todayMillis = remember {
        LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayMillis
            }
        }
    )

    val selectedDate by remember {
        derivedStateOf {
            datePickerState.selectedDateMillis?.let { millis ->
                Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
            }
        }
    }

    val parsedAmount = salaryAmount.replace(",", ".").toDoubleOrNull()
    val isValid = parsedAmount != null && parsedAmount > 0 && selectedDate != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nouveau salaire",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = salaryAmount,
                    onValueChange = { salaryAmount = it },
                    label = { Text("Montant du salaire") },
                    suffix = { Text("€") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = salaryAmount.isNotEmpty() && (parsedAmount == null || parsedAmount <= 0),
                    supportingText = if (salaryAmount.isNotEmpty() && (parsedAmount == null || parsedAmount <= 0)) {
                        { Text("Montant invalide") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                val interactionSource = remember { MutableInteractionSource() }
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release) {
                            showDatePicker = true
                        }
                    }
                }

                OutlinedTextField(
                    value = selectedDate?.format(dateFormatter) ?: "",
                    onValueChange = { },
                    label = { Text("Date du salaire") },
                    placeholder = { Text("Sélectionner une date") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    interactionSource = interactionSource
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(parsedAmount!!, selectedDate!!)
                    }
                },
                enabled = isValid
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showDatePicker = false },
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
