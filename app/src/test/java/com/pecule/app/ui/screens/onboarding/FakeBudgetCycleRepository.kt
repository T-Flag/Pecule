package com.pecule.app.ui.screens.onboarding

import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBudgetCycleRepository {
    private val cycles = MutableStateFlow<List<BudgetCycle>>(emptyList())
    private var nextId = 1L

    val currentCycle: Flow<BudgetCycle?> = cycles.map { list ->
        list.find { it.endDate == null }
    }

    val allCycles: Flow<List<BudgetCycle>> = cycles

    suspend fun insert(cycle: BudgetCycle): Long {
        val id = nextId++
        val newCycle = cycle.copy(id = id)
        cycles.value = cycles.value + newCycle
        return id
    }

    suspend fun update(cycle: BudgetCycle) {
        cycles.value = cycles.value.map { if (it.id == cycle.id) cycle else it }
    }

    suspend fun delete(cycle: BudgetCycle) {
        cycles.value = cycles.value.filter { it.id != cycle.id }
    }

    fun getById(id: Long): Flow<BudgetCycle?> = cycles.map { list ->
        list.find { it.id == id }
    }

    fun getInsertedCycles(): List<BudgetCycle> = cycles.value
}
