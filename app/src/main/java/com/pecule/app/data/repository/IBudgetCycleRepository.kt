package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.flow.Flow

interface IBudgetCycleRepository {
    val currentCycle: Flow<BudgetCycle?>
    val allCycles: Flow<List<BudgetCycle>>
    suspend fun insert(cycle: BudgetCycle): Long
    suspend fun update(cycle: BudgetCycle)
    suspend fun delete(cycle: BudgetCycle)
    fun getById(id: Long): Flow<BudgetCycle?>
}
