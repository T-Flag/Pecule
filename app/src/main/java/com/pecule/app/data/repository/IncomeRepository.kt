package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.IncomeDao
import com.pecule.app.data.local.database.entity.Income
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(
    private val incomeDao: IncomeDao
) {
    suspend fun insert(income: Income): Long = incomeDao.insert(income)
    suspend fun update(income: Income) = incomeDao.update(income)
    suspend fun delete(income: Income) = incomeDao.delete(income)

    fun getById(id: Long): Flow<Income?> = incomeDao.getById(id)
    fun getByCycleId(cycleId: Long): Flow<List<Income>> = incomeDao.getByCycleId(cycleId)
    fun getFixedIncomes(cycleId: Long): Flow<List<Income>> = incomeDao.getFixedIncomes(cycleId)
    fun getVariableIncomes(cycleId: Long): Flow<List<Income>> = incomeDao.getVariableIncomes(cycleId)
}
