package com.pecule.app.ui.screens.statistics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.domain.ExportManager
import com.pecule.app.domain.BalancePoint
import com.pecule.app.ui.components.BalanceHistoryCard
import com.pecule.app.ui.components.CategoryColors
import com.pecule.app.ui.components.DonutChart
import com.pecule.app.ui.components.DonutChartPlaceholder
import com.pecule.app.ui.components.ExportDialog
import com.pecule.app.ui.components.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cycles by viewModel.cycles.collectAsState()
    val selectedCycle by viewModel.selectedCycle.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalIncomes by viewModel.totalIncomes.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val balanceHistory by viewModel.balanceHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    StatisticsContent(
        cycles = cycles,
        selectedCycle = selectedCycle,
        expensesByCategory = expensesByCategory,
        totalExpenses = totalExpenses,
        totalIncomes = totalIncomes,
        balance = balance,
        balanceHistory = balanceHistory,
        isLoading = isLoading,
        onCycleSelected = viewModel::selectCycle,
        onExportClick = { showExportDialog = true },
        modifier = modifier
    )

    if (showExportDialog && selectedCycle != null) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                showExportDialog = false
                scope.launch {
                    val exportData = viewModel.getExportData()
                    val exportManager = ExportManager(context)

                    val intent = withContext(Dispatchers.IO) {
                        when (format) {
                            ExportFormat.CSV -> exportManager.exportCsv(
                                cycle = selectedCycle!!,
                                expenses = exportData.expenses,
                                incomes = exportData.incomes,
                                categories = exportData.categories
                            )
                            ExportFormat.PDF -> exportManager.exportPdf(
                                cycle = selectedCycle!!,
                                expenses = exportData.expenses,
                                incomes = exportData.incomes,
                                categories = exportData.categories
                            )
                        }
                    }

                    context.startActivity(Intent.createChooser(intent, "Exporter"))
                }
            }
        )
    }
}

@Composable
private fun StatisticsContent(
    cycles: List<BudgetCycle>,
    selectedCycle: BudgetCycle?,
    expensesByCategory: Map<CategoryEntity, Double>,
    totalExpenses: Double,
    totalIncomes: Double,
    balance: Double,
    balanceHistory: List<BalancePoint>,
    isLoading: Boolean = false,
    onCycleSelected: (BudgetCycle) -> Unit,
    onExportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CycleSelector(
                cycles = cycles,
                selectedCycle = selectedCycle,
                onCycleSelected = onCycleSelected,
                modifier = Modifier.weight(1f)
            )

            if (selectedCycle != null) {
                IconButton(onClick = onExportClick) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exporter",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            DonutChartPlaceholder()
        } else {
            DonutChart(
                data = expensesByCategory,
                size = 220.dp,
                strokeWidth = 36.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (expensesByCategory.isEmpty()) {
            Text(
                text = "Aucune dépense ce mois",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            CategoryLegend(
                expensesByCategory = expensesByCategory,
                totalExpenses = totalExpenses
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SummaryCard(
            totalExpenses = totalExpenses,
            totalIncomes = totalIncomes,
            balance = balance
        )

        Spacer(modifier = Modifier.height(16.dp))

        BalanceHistoryCard(
            balanceHistory = balanceHistory
        )
    }
}

@Composable
private fun CycleSelector(
    cycles: List<BudgetCycle>,
    selectedCycle: BudgetCycle?,
    onCycleSelected: (BudgetCycle) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRANCE)

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCycle?.startDate?.format(monthFormatter)?.replaceFirstChar {
                        it.titlecase(Locale.FRANCE)
                    } ?: "Sélectionner un cycle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Ouvrir le menu"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            cycles.forEach { cycle ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = cycle.startDate.format(monthFormatter).replaceFirstChar {
                                it.titlecase(Locale.FRANCE)
                            }
                        )
                    },
                    onClick = {
                        onCycleSelected(cycle)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryLegend(
    expensesByCategory: Map<CategoryEntity, Double>,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    val sortedCategories = expensesByCategory.entries.sortedByDescending { it.value }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            sortedCategories.forEachIndexed { index, (category, amount) ->
                val percentage = if (totalExpenses > 0) (amount / totalExpenses * 100) else 0.0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(CategoryColors.getColor(category))
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = currencyFormat.format(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = String.format(Locale.FRANCE, "%.1f%%", percentage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(48.dp)
                    )
                }

                if (index < sortedCategories.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    totalExpenses: Double,
    totalIncomes: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Résumé",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            SummaryRow(
                label = "Total dépenses",
                value = currencyFormat.format(totalExpenses),
                valueColor = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            SummaryRow(
                label = "Total revenus",
                value = currencyFormat.format(totalIncomes),
                valueColor = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            SummaryRow(
                label = "Solde du cycle",
                value = currencyFormat.format(balance),
                valueColor = if (balance >= 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                isBold = true
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color,
    isBold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
