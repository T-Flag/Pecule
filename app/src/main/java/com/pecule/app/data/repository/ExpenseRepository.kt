package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.ExpenseDao
import com.pecule.app.data.local.database.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) : IExpenseRepository {
    override suspend fun insert(expense: Expense): Long = expenseDao.insert(expense)
    override suspend fun update(expense: Expense) = expenseDao.update(expense)
    override suspend fun delete(expense: Expense) = expenseDao.delete(expense)

    override fun getById(id: Long): Flow<Expense?> = expenseDao.getById(id)
    override fun getByCycleId(cycleId: Long): Flow<List<Expense>> = expenseDao.getByCycleId(cycleId)
    override fun getFixedExpenses(cycleId: Long): Flow<List<Expense>> = expenseDao.getFixedExpenses(cycleId)
    override fun getVariableExpenses(cycleId: Long): Flow<List<Expense>> = expenseDao.getVariableExpenses(cycleId)
}
