package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.IncomeDao
import com.pecule.app.data.local.database.entity.Income
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor(
    private val incomeDao: IncomeDao
) : IIncomeRepository {
    override suspend fun insert(income: Income): Long = incomeDao.insert(income)
    override suspend fun update(income: Income) = incomeDao.update(income)
    override suspend fun delete(income: Income) = incomeDao.delete(income)

    override fun getById(id: Long): Flow<Income?> = incomeDao.getById(id)
    override fun getByCycleId(cycleId: Long): Flow<List<Income>> = incomeDao.getByCycleId(cycleId)
    override fun getFixedIncomes(cycleId: Long): Flow<List<Income>> = incomeDao.getFixedIncomes(cycleId)
    override fun getVariableIncomes(cycleId: Long): Flow<List<Income>> = incomeDao.getVariableIncomes(cycleId)
}
