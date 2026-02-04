package com.pecule.app.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.ICategoryRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val budgetCycleRepository: IBudgetCycleRepository,
    private val expenseRepository: IExpenseRepository,
    private val incomeRepository: IIncomeRepository,
    private val categoryRepository: ICategoryRepository
) : ViewModel() {

    val cycles: StateFlow<List<BudgetCycle>> = budgetCycleRepository.allCycles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCycle = MutableStateFlow<BudgetCycle?>(null)
    val selectedCycle: StateFlow<BudgetCycle?> = _selectedCycle.asStateFlow()

    private val categories = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            budgetCycleRepository.currentCycle.collect { cycle ->
                if (_selectedCycle.value == null) {
                    _selectedCycle.value = cycle
                }
            }
        }
    }

    fun selectCycle(cycle: BudgetCycle) {
        _selectedCycle.value = cycle
    }

    private val expensesForSelectedCycle = _selectedCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.getByCycleId(cycle.id)
            }
        }

    private val incomesForSelectedCycle = _selectedCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(emptyList())
            } else {
                incomeRepository.getByCycleId(cycle.id)
            }
        }

    val expensesByCategory: StateFlow<Map<CategoryEntity, Double>> = combine(
        expensesForSelectedCycle,
        categories
    ) { expenses, categoryList ->
        val categoryMap = categoryList.associateBy { it.id }
        expenses
            .groupBy { expense -> expense.categoryId?.let { categoryMap[it] } }
            .filterKeys { it != null }
            .mapKeys { it.key!! }
            .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    val totalExpenses: StateFlow<Double> = expensesForSelectedCycle
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalIncomes: StateFlow<Double> = incomesForSelectedCycle
        .map { incomes -> incomes.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val balance: StateFlow<Double> = combine(
        _selectedCycle,
        totalExpenses,
        totalIncomes
    ) { cycle, expenses, incomes ->
        (cycle?.amount ?: 0.0) + incomes - expenses
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
}
