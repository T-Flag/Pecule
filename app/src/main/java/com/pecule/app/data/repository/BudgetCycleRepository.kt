package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.BudgetCycleDao
import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetCycleRepository @Inject constructor(
    private val budgetCycleDao: BudgetCycleDao
) {
    val currentCycle: Flow<BudgetCycle?> = budgetCycleDao.getCurrentCycle()
    val allCycles: Flow<List<BudgetCycle>> = budgetCycleDao.getAllCycles()

    suspend fun insert(cycle: BudgetCycle): Long = budgetCycleDao.insert(cycle)
    suspend fun update(cycle: BudgetCycle) = budgetCycleDao.update(cycle)
    suspend fun delete(cycle: BudgetCycle) = budgetCycleDao.delete(cycle)
    fun getById(id: Long): Flow<BudgetCycle?> = budgetCycleDao.getById(id)
}
