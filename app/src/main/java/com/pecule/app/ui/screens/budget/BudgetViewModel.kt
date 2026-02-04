package com.pecule.app.ui.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetCycleRepository: IBudgetCycleRepository,
    val expenseRepository: IExpenseRepository,
    val incomeRepository: IIncomeRepository,
    val categoryRepository: ICategoryRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val fixedExpenses: StateFlow<List<Expense>> = budgetCycleRepository.currentCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.getFixedExpenses(cycle.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val variableExpenses: StateFlow<List<Expense>> = budgetCycleRepository.currentCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(emptyList())
            } else {
                expenseRepository.getVariableExpenses(cycle.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomes: StateFlow<List<Income>> = budgetCycleRepository.currentCycle
        .flatMapLatest { cycle ->
            if (cycle == null) {
                flowOf(emptyList())
            } else {
                incomeRepository.getByCycleId(cycle.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalFixed: StateFlow<Double> = fixedExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalVariable: StateFlow<Double> = variableExpenses
        .map { expenses -> expenses.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalIncomes: StateFlow<Double> = incomes
        .map { incomeList -> incomeList.sumOf { it.amount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

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
