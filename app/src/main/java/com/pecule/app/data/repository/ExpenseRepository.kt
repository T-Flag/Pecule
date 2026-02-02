package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.ExpenseDao
import com.pecule.app.data.local.database.entity.Expense
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun insert(expense: Expense): Long = expenseDao.insert(expense)
    suspend fun update(expense: Expense) = expenseDao.update(expense)
    suspend fun delete(expense: Expense) = expenseDao.delete(expense)

    fun getById(id: Long): Flow<Expense?> = expenseDao.getById(id)
    fun getByCycleId(cycleId: Long): Flow<List<Expense>> = expenseDao.getByCycleId(cycleId)
    fun getFixedExpenses(cycleId: Long): Flow<List<Expense>> = expenseDao.getFixedExpenses(cycleId)
    fun getVariableExpenses(cycleId: Long): Flow<List<Expense>> = expenseDao.getVariableExpenses(cycleId)
}
