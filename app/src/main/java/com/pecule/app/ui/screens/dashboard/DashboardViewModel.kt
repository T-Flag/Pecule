package com.pecule.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.ICategoryRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.data.repository.IUserPreferencesRepository
import com.pecule.app.domain.BalanceCalculator
import com.pecule.app.domain.BudgetAlert
import com.pecule.app.domain.BudgetAlertCalculator
import com.pecule.app.domain.BudgetAlertLevel
import com.pecule.app.domain.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val budgetCycleRepository: IBudgetCycleRepository,
    val expenseRepository: IExpenseRepository,
    val incomeRepository: IIncomeRepository,
    private val categoryRepository: ICategoryRepository,
    private val balanceCalculator: BalanceCalculator
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            budgetCycleRepository.currentCycle.collect { cycle ->
                if (cycle != null || _isLoading.value) {
                    _isLoading.value = false
                }
            }
        }
    }

    val userName: StateFlow<String> = userPreferencesRepository.userPreferences
        .map { it.firstName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val currentCycleId: StateFlow<Long?> = budgetCycleRepository.currentCycle
        .map { it?.id }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    private val budgetAlertCalculator = BudgetAlertCalculator()

    val budgetAlert: StateFlow<BudgetAlert> = budgetPercentageUsed
        .map { percentage -> budgetAlertCalculator.calculate((percentage * 100).toDouble()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetAlert(BudgetAlertLevel.NONE, "", 0)
        )

    val recentTransactions: StateFlow<List<Transaction>> = combine(
        currentCycleWithTransactions,
        categories
    ) { (_, expenses, incomes), categoryList ->
        val categoryMap = categoryList.associateBy { it.id }

        val variableExpenses = expenses
            .filter { !it.isFixed }
            .map { expense ->
                Transaction(
                    id = expense.id,
                    label = expense.label,
                    amount = expense.amount,
                    date = expense.date,
                    isExpense = true,
                    isFixed = expense.isFixed,
                    category = expense.categoryId?.let { categoryMap[it] }
                )
            }

        val variableIncomes = incomes
            .filter { !it.isFixed }
            .map { income ->
                Transaction(
                    id = income.id,
                    label = income.label,
                    amount = income.amount,
                    date = income.date,
                    isExpense = false,
                    isFixed = income.isFixed,
                    category = null
                )
            }

        (variableExpenses + variableIncomes)
            .sortedByDescending { it.date }
            .take(5)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.delete(expense)
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            incomeRepository.delete(income)
        }
    }

    fun getCategoryById(categoryId: Long?): CategoryEntity? {
        return categoryId?.let { id -> categories.value.find { it.id == id } }
    }
}
