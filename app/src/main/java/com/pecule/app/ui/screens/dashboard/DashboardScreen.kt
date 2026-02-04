package com.pecule.app.ui.screens.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.domain.CategoryInitializer
import com.pecule.app.domain.Transaction
import com.pecule.app.domain.BudgetAlert
import com.pecule.app.domain.BudgetAlertLevel
import com.pecule.app.ui.components.AddTransactionDialog
import com.pecule.app.ui.components.AddTransactionUiState
import com.pecule.app.ui.components.AddTransactionViewModel
import com.pecule.app.ui.components.BalanceCard
import com.pecule.app.ui.components.BalanceCardPlaceholder
import com.pecule.app.ui.components.BudgetAlertBanner
import com.pecule.app.ui.components.DeleteConfirmationDialog
import com.pecule.app.ui.components.EmptyStateView
import com.pecule.app.ui.components.SwipeableTransactionItem
import com.pecule.app.ui.components.TransactionItem
import com.pecule.app.ui.components.TransactionListPlaceholder
import com.pecule.app.ui.theme.PeculeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val currentBalance by viewModel.currentBalance.collectAsStateWithLifecycle()
    val budgetPercentageUsed by viewModel.budgetPercentageUsed.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val currentCycleId by viewModel.currentCycleId.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val budgetAlert by viewModel.budgetAlert.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    var showFabMenu by remember { mutableStateOf(false) }
    var alertDismissed by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isExpenseDialog by remember { mutableStateOf(true) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Context menu and delete confirmation state
    var contextMenuTransaction by remember { mutableStateOf<Transaction?>(null) }
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (currentCycleId != null) {
                Box {
                    FloatingActionButton(
                        onClick = { showFabMenu = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Ajouter une transaction"
                        )
                    }

                    DropdownMenu(
                        expanded = showFabMenu,
                        onDismissRequest = { showFabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Dépense") },
                            onClick = {
                                showFabMenu = false
                                isExpenseDialog = true
                                showAddDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Revenu") },
                            onClick = {
                                showFabMenu = false
                                isExpenseDialog = false
                                showAddDialog = true
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        DashboardContent(
            userName = userName,
            currentBalance = currentBalance,
            budgetPercentageUsed = budgetPercentageUsed,
            budgetAlert = budgetAlert,
            alertVisible = !alertDismissed,
            onAlertDismiss = { alertDismissed = true },
            recentTransactions = recentTransactions,
            isLoading = isLoading,
            onNavigateToProfile = onNavigateToProfile,
            onAddExpense = {
                isExpenseDialog = true
                showAddDialog = true
            },
            contextMenuTransaction = contextMenuTransaction,
            onContextMenuOpen = { contextMenuTransaction = it },
            onContextMenuDismiss = { contextMenuTransaction = null },
            onDeleteRequest = { transaction ->
                contextMenuTransaction = null
                transactionToDelete = transaction
            },
            onEditRequest = { transaction ->
                contextMenuTransaction = null
                editingTransaction = transaction
                isExpenseDialog = transaction.isExpense
                showAddDialog = true
            },
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Delete Confirmation Dialog
    transactionToDelete?.let { transaction ->
        DeleteConfirmationDialog(
            transactionLabel = transaction.label,
            onConfirm = {
                scope.launch {
                    if (transaction.isExpense) {
                        val expense = viewModel.expenseRepository.getById(transaction.id).first()
                        expense?.let { viewModel.deleteExpense(it) }
                    } else {
                        val income = viewModel.incomeRepository.getById(transaction.id).first()
                        income?.let { viewModel.deleteIncome(it) }
                    }
                }
                transactionToDelete = null
            },
            onDismiss = {
                transactionToDelete = null
            }
        )
    }

    // Add/Edit Transaction Dialog
    if (showAddDialog && currentCycleId != null) {
        val dialogScope = rememberCoroutineScope()
        var errors by remember { mutableStateOf<List<String>>(emptyList()) }

        val addViewModel = remember(isExpenseDialog, currentCycleId, editingTransaction) {
            AddTransactionViewModel(
                expenseRepository = viewModel.expenseRepository,
                incomeRepository = viewModel.incomeRepository,
                isExpense = isExpenseDialog,
                cycleId = currentCycleId!!,
                existingTransaction = editingTransaction
            )
        }

        var uiState by remember { mutableStateOf(AddTransactionUiState(isExpense = isExpenseDialog)) }
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
            categories = categories,
            onLabelChange = { addViewModel.updateLabel(it) },
            onAmountChange = { addViewModel.updateAmount(it) },
            onCategoryChange = { addViewModel.updateCategory(it) },
            onDateChange = { addViewModel.updateDate(it) },
            onIsFixedChange = { addViewModel.toggleIsFixed() },
            onDismiss = {
                showAddDialog = false
                editingTransaction = null
                errors = emptyList()
            },
            onSave = {
                dialogScope.launch {
                    val result = addViewModel.save()
                    if (result.isSuccess) {
                        showAddDialog = false
                        editingTransaction = null
                        errors = emptyList()
                    } else {
                        errors = result.errors
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DashboardContent(
    userName: String,
    currentBalance: Double,
    budgetPercentageUsed: Float,
    budgetAlert: BudgetAlert = BudgetAlert(BudgetAlertLevel.NONE, "", 0),
    alertVisible: Boolean = true,
    onAlertDismiss: () -> Unit = {},
    recentTransactions: List<Transaction>,
    isLoading: Boolean = false,
    onNavigateToProfile: () -> Unit,
    onAddExpense: () -> Unit = {},
    contextMenuTransaction: Transaction? = null,
    onContextMenuOpen: (Transaction) -> Unit = {},
    onContextMenuDismiss: () -> Unit = {},
    onDeleteRequest: (Transaction) -> Unit = {},
    onEditRequest: (Transaction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (userName.isNotEmpty()) "Bonjour, $userName" else "Bonjour",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            IconButton(onClick = onNavigateToProfile) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Balance Card
        if (isLoading) {
            BalanceCardPlaceholder()
        } else {
            BalanceCard(
                balance = currentBalance,
                percentageUsed = budgetPercentageUsed
            )
        }

        // Budget Alert Banner
        if (budgetAlert.level != BudgetAlertLevel.NONE) {
            Spacer(modifier = Modifier.height(12.dp))
            BudgetAlertBanner(
                alert = budgetAlert,
                visible = alertVisible,
                onDismiss = onAlertDismiss
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions Section
        Text(
            text = "Dernières activités",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            TransactionListPlaceholder(itemCount = 3)
        } else if (recentTransactions.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.AccountBalanceWallet,
                title = "Rien à afficher",
                subtitle = "Ajoutez votre première dépense avec le bouton +",
                actionLabel = "Ajouter une dépense",
                onAction = onAddExpense
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = recentTransactions,
                    key = { "${it.id}_${it.isExpense}" }
                ) { transaction ->
                    SwipeableTransactionItem(
                        transaction = transaction,
                        onSwipeToDelete = { onDeleteRequest(transaction) },
                        onSwipeToEdit = { onEditRequest(transaction) }
                    ) {
                        Box {
                            TransactionItem(
                                transaction = transaction,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = { },
                                        onLongClick = { onContextMenuOpen(transaction) }
                                    )
                            )

                            DropdownMenu(
                                expanded = contextMenuTransaction?.id == transaction.id &&
                                        contextMenuTransaction?.isExpense == transaction.isExpense,
                                onDismissRequest = onContextMenuDismiss
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Modifier") },
                                    onClick = { onEditRequest(transaction) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer") },
                                    onClick = { onDeleteRequest(transaction) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
    val foodCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Alimentation" }
    val transportCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Transport" }
    PeculeTheme {
        DashboardContent(
            userName = "Marie",
            currentBalance = 1847.32,
            budgetPercentageUsed = 0.35f,
            recentTransactions = listOf(
                Transaction(
                    id = 1,
                    label = "Courses Carrefour",
                    amount = 85.50,
                    date = LocalDate.of(2025, 1, 28),
                    isExpense = true,
                    isFixed = false,
                    category = foodCategory
                ),
                Transaction(
                    id = 2,
                    label = "Vente Vinted",
                    amount = 45.00,
                    date = LocalDate.of(2025, 1, 27),
                    isExpense = false,
                    isFixed = false,
                    category = null
                ),
                Transaction(
                    id = 3,
                    label = "Essence",
                    amount = 62.00,
                    date = LocalDate.of(2025, 1, 26),
                    isExpense = true,
                    isFixed = false,
                    category = transportCategory
                )
            ),
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentEmptyPreview() {
    PeculeTheme {
        DashboardContent(
            userName = "Jean",
            currentBalance = 2500.00,
            budgetPercentageUsed = 0f,
            recentTransactions = emptyList(),
            onNavigateToProfile = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DashboardContentDarkPreview() {
    val entertainmentCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Loisirs" }
    PeculeTheme(darkTheme = true) {
        DashboardContent(
            userName = "Pierre",
            currentBalance = 956.80,
            budgetPercentageUsed = 0.72f,
            recentTransactions = listOf(
                Transaction(
                    id = 1,
                    label = "Restaurant",
                    amount = 38.00,
                    date = LocalDate.of(2025, 1, 28),
                    isExpense = true,
                    isFixed = false,
                    category = entertainmentCategory
                )
            ),
            onNavigateToProfile = {}
        )
    }
}
