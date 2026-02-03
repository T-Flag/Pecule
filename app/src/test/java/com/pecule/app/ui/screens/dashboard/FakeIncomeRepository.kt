package com.pecule.app.ui.screens.dashboard

import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IIncomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeIncomeRepository : IIncomeRepository {
    private val incomes = MutableStateFlow<List<Income>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(income: Income): Long {
        val id = nextId++
        val newIncome = income.copy(id = id)
        incomes.value = incomes.value + newIncome
        return id
    }

    override suspend fun update(income: Income) {
        incomes.value = incomes.value.map { if (it.id == income.id) income else it }
    }

    override suspend fun delete(income: Income) {
        incomes.value = incomes.value.filter { it.id != income.id }
    }

    override fun getById(id: Long): Flow<Income?> = incomes.map { list ->
        list.find { it.id == id }
    }

    override fun getByCycleId(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId }
    }

    override fun getFixedIncomes(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId && it.isFixed }
    }

    override fun getVariableIncomes(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId && !it.isFixed }
    }

    fun setIncomes(list: List<Income>) {
        incomes.value = list
    }
}
