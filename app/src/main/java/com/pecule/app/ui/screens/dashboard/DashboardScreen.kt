package com.pecule.app.ui.screens.dashboard

import androidx.compose.foundation.background
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
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.domain.Transaction
import com.pecule.app.ui.components.AddTransactionDialog
import com.pecule.app.ui.components.AddTransactionUiState
import com.pecule.app.ui.components.AddTransactionViewModel
import com.pecule.app.ui.components.BalanceCard
import com.pecule.app.ui.components.TransactionItem
import com.pecule.app.ui.theme.PeculeTheme
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

    var showFabMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isExpenseDialog by remember { mutableStateOf(true) }

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
            recentTransactions = recentTransactions,
            onNavigateToProfile = onNavigateToProfile,
            modifier = Modifier.padding(paddingValues)
        )
    }

    // Add Transaction Dialog
    if (showAddDialog && currentCycleId != null) {
        val scope = rememberCoroutineScope()
        var errors by remember { mutableStateOf<List<String>>(emptyList()) }

        val addViewModel = remember(isExpenseDialog, currentCycleId) {
            AddTransactionViewModel(
                expenseRepository = viewModel.expenseRepository,
                incomeRepository = viewModel.incomeRepository,
                isExpense = isExpenseDialog,
                cycleId = currentCycleId!!,
                existingTransaction = null
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
            onLabelChange = { addViewModel.updateLabel(it) },
            onAmountChange = { addViewModel.updateAmount(it) },
            onCategoryChange = { addViewModel.updateCategory(it) },
            onDateChange = { addViewModel.updateDate(it) },
            onIsFixedChange = { addViewModel.toggleIsFixed() },
            onDismiss = {
                showAddDialog = false
                errors = emptyList()
            },
            onSave = {
                scope.launch {
                    val result = addViewModel.save()
                    if (result.isSuccess) {
                        showAddDialog = false
                        errors = emptyList()
                    } else {
                        errors = result.errors
                    }
                }
            }
        )
    }
}

@Composable
private fun DashboardContent(
    userName: String,
    currentBalance: Double,
    budgetPercentageUsed: Float,
    recentTransactions: List<Transaction>,
    onNavigateToProfile: () -> Unit,
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
        BalanceCard(
            balance = currentBalance,
            percentageUsed = budgetPercentageUsed
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Transactions Section
        Text(
            text = "Dernières activités",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (recentTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune activité récente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = recentTransactions,
                    key = { "${it.id}_${it.isExpense}" }
                ) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardContentPreview() {
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
                    category = Category.FOOD
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
                    category = Category.TRANSPORT
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
                    category = Category.ENTERTAINMENT
                )
            ),
            onNavigateToProfile = {}
        )
    }
}
