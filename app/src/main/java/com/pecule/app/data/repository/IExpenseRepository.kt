package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.Expense
import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    suspend fun insert(expense: Expense): Long
    suspend fun update(expense: Expense)
    suspend fun delete(expense: Expense)
    fun getById(id: Long): Flow<Expense?>
    fun getByCycleId(cycleId: Long): Flow<List<Expense>>
    fun getFixedExpenses(cycleId: Long): Flow<List<Expense>>
    fun getVariableExpenses(cycleId: Long): Flow<List<Expense>>
}
