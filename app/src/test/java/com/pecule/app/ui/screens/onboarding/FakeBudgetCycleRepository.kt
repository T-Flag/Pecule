package com.pecule.app.ui.screens.onboarding

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.repository.IBudgetCycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBudgetCycleRepository : IBudgetCycleRepository {
    private val cycles = MutableStateFlow<List<BudgetCycle>>(emptyList())
    private var nextId = 1L

    override val currentCycle: Flow<BudgetCycle?> = cycles.map { list ->
        list.find { it.endDate == null }
    }

    override val allCycles: Flow<List<BudgetCycle>> = cycles

    override suspend fun insert(cycle: BudgetCycle): Long {
        val id = nextId++
        val newCycle = cycle.copy(id = id)
        cycles.value = cycles.value + newCycle
        return id
    }

    override suspend fun update(cycle: BudgetCycle) {
        cycles.value = cycles.value.map { if (it.id == cycle.id) cycle else it }
    }

    override suspend fun delete(cycle: BudgetCycle) {
        cycles.value = cycles.value.filter { it.id != cycle.id }
    }

    override fun getById(id: Long): Flow<BudgetCycle?> = cycles.map { list ->
        list.find { it.id == id }
    }

    fun getInsertedCycles(): List<BudgetCycle> = cycles.value
}
