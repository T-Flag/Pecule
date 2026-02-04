package com.pecule.app.ui.screens.budget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.domain.Transaction
import com.pecule.app.ui.components.AddTransactionDialog
import com.pecule.app.ui.components.AddTransactionUiState
import com.pecule.app.ui.components.AddTransactionViewModel
import com.pecule.app.ui.components.DeleteConfirmationDialog
import com.pecule.app.ui.components.SwipeableTransactionItem
import com.pecule.app.ui.components.TransactionItem
import com.pecule.app.ui.theme.PeculeTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private val tabs = listOf("Fixe", "Variable", "Revenus")

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val fixedExpenses by viewModel.fixedExpenses.collectAsStateWithLifecycle()
    val variableExpenses by viewModel.variableExpenses.collectAsStateWithLifecycle()
    val incomes by viewModel.incomes.collectAsStateWithLifecycle()
    val totalFixed by viewModel.totalFixed.collectAsStateWithLifecycle()
    val totalVariable by viewModel.totalVariable.collectAsStateWithLifecycle()
    val totalIncomes by viewModel.totalIncomes.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Context menu state
    var contextMenuExpense by remember { mutableStateOf<Expense?>(null) }
    var contextMenuIncome by remember { mutableStateOf<Income?>(null) }

    // Delete confirmation state
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var incomeToDelete by remember { mutableStateOf<Income?>(null) }

    val currentTotal = when (selectedTab) {
        0 -> totalFixed
        1 -> totalVariable
        else -> totalIncomes
    }

    val emptyMessage = when (selectedTab) {
        0 -> "Aucune dépense fixe"
        1 -> "Aucune dépense variable"
        else -> "Aucun revenu"
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Ajouter"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Total
            Text(
                text = "Total : ${formatCurrency(currentTotal)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )

            // Content based on selected tab
            when (selectedTab) {
                0 -> ExpenseList(
                    expenses = fixedExpenses,
                    emptyMessage = emptyMessage,
                    contextMenuExpense = contextMenuExpense,
                    onContextMenuOpen = { contextMenuExpense = it },
                    onContextMenuDismiss = { contextMenuExpense = null },
                    onEdit = { expense ->
                        contextMenuExpense = null
                        editingTransaction = expense.toTransaction()
                        showAddDialog = true
                    },
                    onDelete = { expense ->
                        contextMenuExpense = null
                        expenseToDelete = expense
                    }
                )
                1 -> ExpenseList(
                    expenses = variableExpenses,
                    emptyMessage = emptyMessage,
                    contextMenuExpense = contextMenuExpense,
                    onContextMenuOpen = { contextMenuExpense = it },
                    onContextMenuDismiss = { contextMenuExpense = null },
                    onEdit = { expense ->
                        contextMenuExpense = null
                        editingTransaction = expense.toTransaction()
                        showAddDialog = true
                    },
                    onDelete = { expense ->
                        contextMenuExpense = null
                        expenseToDelete = expense
                    }
                )
                2 -> IncomeList(
                    incomes = incomes,
                    emptyMessage = emptyMessage,
                    contextMenuIncome = contextMenuIncome,
                    onContextMenuOpen = { contextMenuIncome = it },
                    onContextMenuDismiss = { contextMenuIncome = null },
                    onEdit = { income ->
                        contextMenuIncome = null
                        editingTransaction = income.toTransaction()
                        showAddDialog = true
                    },
                    onDelete = { income ->
                        contextMenuIncome = null
                        incomeToDelete = income
                    }
                )
            }
        }
    }

    // Add/Edit Transaction Dialog
    if (showAddDialog) {
        val isExpense = selectedTab != 2
        val isFixed = selectedTab == 0

        AddTransactionDialogWrapper(
            viewModel = viewModel,
            isExpense = isExpense,
            isFixed = isFixed,
            existingTransaction = editingTransaction,
            onDismiss = {
                showAddDialog = false
                editingTransaction = null
            },
            onSaveSuccess = {
                showAddDialog = false
                editingTransaction = null
            }
        )
    }

    // Delete Confirmation Dialog for Expense
    expenseToDelete?.let { expense ->
        DeleteConfirmationDialog(
            transactionLabel = expense.label,
            onConfirm = {
                viewModel.deleteExpense(expense)
                expenseToDelete = null
            },
            onDismiss = {
                expenseToDelete = null
            }
        )
    }

    // Delete Confirmation Dialog for Income
    incomeToDelete?.let { income ->
        DeleteConfirmationDialog(
            transactionLabel = income.label,
            onConfirm = {
                viewModel.deleteIncome(income)
                incomeToDelete = null
            },
            onDismiss = {
                incomeToDelete = null
            }
        )
    }
}

@Composable
private fun AddTransactionDialogWrapper(
    viewModel: BudgetViewModel,
    isExpense: Boolean,
    isFixed: Boolean,
    existingTransaction: Transaction?,
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }

    // We need the cycle ID - we'll get it from the first expense or income, or use 1 as default
    // In a real app, we'd get this from the ViewModel
    val cycleId = viewModel.fixedExpenses.collectAsStateWithLifecycle().value.firstOrNull()?.cycleId
        ?: viewModel.variableExpenses.collectAsStateWithLifecycle().value.firstOrNull()?.cycleId
        ?: viewModel.incomes.collectAsStateWithLifecycle().value.firstOrNull()?.cycleId
        ?: 1L

    val addViewModel = remember(isExpense, existingTransaction, cycleId) {
        AddTransactionViewModel(
            expenseRepository = viewModel.expenseRepository,
            incomeRepository = viewModel.incomeRepository,
            isExpense = isExpense,
            cycleId = cycleId,
            existingTransaction = existingTransaction
        )
    }

    // Pre-set isFixed for new transactions
    LaunchedEffect(addViewModel, isFixed, existingTransaction) {
        if (existingTransaction == null && isExpense) {
            if (isFixed && !addViewModel.uiState.value.isFixed) {
                addViewModel.toggleIsFixed()
            } else if (!isFixed && addViewModel.uiState.value.isFixed) {
                addViewModel.toggleIsFixed()
            }
        }
    }

    var uiState by remember { mutableStateOf(AddTransactionUiState(isExpense = isExpense)) }
    LaunchedEffect(addViewModel) {
        addViewModel.uiState.collect { uiState = it }
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
        onLabelChange = { addViewModel.updateLabel(it) },
        onAmountChange = { addViewModel.updateAmount(it) },
        onCategoryChange = { addViewModel.updateCategory(it) },
        onDateChange = { addViewModel.updateDate(it) },
        onIsFixedChange = { addViewModel.toggleIsFixed() },
        onDismiss = onDismiss,
        onSave = {
            scope.launch {
                val result = addViewModel.save()
                if (result.isSuccess) {
                    onSaveSuccess()
                } else {
                    errors = result.errors
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpenseList(
    expenses: List<Expense>,
    emptyMessage: String,
    contextMenuExpense: Expense?,
    onContextMenuOpen: (Expense) -> Unit,
    onContextMenuDismiss: () -> Unit,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = expenses,
                key = { it.id }
            ) { expense ->
                SwipeableTransactionItem(
                    transaction = expense.toTransaction(),
                    onSwipeToDelete = { onDelete(expense) }
                ) {
                    Box {
                        TransactionItem(
                            transaction = expense.toTransaction(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { onContextMenuOpen(expense) }
                                )
                                .padding(horizontal = 16.dp)
                        )

                        DropdownMenu(
                            expanded = contextMenuExpense?.id == expense.id,
                            onDismissRequest = onContextMenuDismiss
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modifier") },
                                onClick = { onEdit(expense) }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer") },
                                onClick = { onDelete(expense) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IncomeList(
    incomes: List<Income>,
    emptyMessage: String,
    contextMenuIncome: Income?,
    onContextMenuOpen: (Income) -> Unit,
    onContextMenuDismiss: () -> Unit,
    onEdit: (Income) -> Unit,
    onDelete: (Income) -> Unit
) {
    if (incomes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = incomes,
                key = { it.id }
            ) { income ->
                SwipeableTransactionItem(
                    transaction = income.toTransaction(),
                    onSwipeToDelete = { onDelete(income) }
                ) {
                    Box {
                        TransactionItem(
                            transaction = income.toTransaction(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { onContextMenuOpen(income) }
                                )
                                .padding(horizontal = 16.dp)
                        )

                        DropdownMenu(
                            expanded = contextMenuIncome?.id == income.id,
                            onDismissRequest = onContextMenuDismiss
                        ) {
                            DropdownMenuItem(
                                text = { Text("Modifier") },
                                onClick = { onEdit(income) }
                            )
                            DropdownMenuItem(
                                text = { Text("Supprimer") },
                                onClick = { onDelete(income) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Expense.toTransaction() = Transaction(
    id = id,
    label = label,
    amount = amount,
    date = date,
    isExpense = true,
    isFixed = isFixed,
    category = category
)

private fun Income.toTransaction() = Transaction(
    id = id,
    label = label,
    amount = amount,
    date = date,
    isExpense = false,
    isFixed = isFixed,
    category = null
)

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    return formatter.format(amount)
}

@Preview(showBackground = true)
@Composable
private fun BudgetScreenContentPreview() {
    PeculeTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = 0) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = index == 0,
                        onClick = { },
                        text = { Text(title) }
                    )
                }
            }
            Text(
                text = "Total : 900,00 €",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(
                    listOf(
                        Transaction(1, "Loyer", 800.0, LocalDate.now(), true, true, Category.HOUSING),
                        Transaction(2, "Internet", 30.0, LocalDate.now(), true, true, Category.UTILITIES),
                        Transaction(3, "Électricité", 70.0, LocalDate.now(), true, true, Category.UTILITIES)
                    )
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
