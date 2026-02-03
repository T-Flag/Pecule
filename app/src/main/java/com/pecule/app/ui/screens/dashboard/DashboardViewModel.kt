package com.pecule.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.data.repository.IUserPreferencesRepository
import com.pecule.app.domain.BalanceCalculator
import com.pecule.app.domain.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val budgetCycleRepository: IBudgetCycleRepository,
    private val expenseRepository: IExpenseRepository,
    private val incomeRepository: IIncomeRepository,
    private val balanceCalculator: BalanceCalculator
) : ViewModel() {

    val userName: StateFlow<String> = userPreferencesRepository.userPreferences
        .map { it.firstName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    private val currentCycleWithTransactions = budgetCycleRepository.currentCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(Triple<BudgetCycle?, List<Expense>, List<Income>>(null, emptyList(), emptyList()))
            } else {
                combine(
                    flowOf(cycle),
                    expenseRepository.getByCycleId(cycle.id),
                    incomeRepository.getByCycleId(cycle.id)
                ) { c, expenses, incomes ->
                    Triple(c, expenses, incomes)
                }
            }
        }

    val currentBalance: StateFlow<Double> = currentCycleWithTransactions
        .map { (cycle, expenses, incomes) ->
            if (cycle == null) 0.0
            else balanceCalculator.calculateBalance(cycle, expenses, incomes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val budgetPercentageUsed: StateFlow<Float> = currentCycleWithTransactions
        .map { (cycle, expenses, incomes) ->
            if (cycle == null) 0f
            else balanceCalculator.calculatePercentageUsed(cycle, expenses, incomes)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    val recentTransactions: StateFlow<List<Transaction>> = currentCycleWithTransactions
        .map { (_, expenses, incomes) ->
            val variableExpenses = expenses
                .filter { !it.isFixed }
                .map { it.toTransaction() }

            val variableIncomes = incomes
                .filter { !it.isFixed }
                .map { it.toTransaction() }

            (variableExpenses + variableIncomes)
                .sortedByDescending { it.date }
                .take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
}
