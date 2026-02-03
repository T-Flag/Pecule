package com.pecule.app.ui.screens.dashboard

import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.repository.IExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeExpenseRepository : IExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(expense: Expense): Long {
        val id = nextId++
        val newExpense = expense.copy(id = id)
        expenses.value = expenses.value + newExpense
        return id
    }

    override suspend fun update(expense: Expense) {
        expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun delete(expense: Expense) {
        expenses.value = expenses.value.filter { it.id != expense.id }
    }

    override fun getById(id: Long): Flow<Expense?> = expenses.map { list ->
        list.find { it.id == id }
    }

    override fun getByCycleId(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId }
    }

    override fun getFixedExpenses(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId && it.isFixed }
    }

    override fun getVariableExpenses(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId && !it.isFixed }
    }

    fun setExpenses(list: List<Expense>) {
        expenses.value = list
    }
}
