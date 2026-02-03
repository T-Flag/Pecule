package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.Income
import kotlinx.coroutines.flow.Flow

interface IIncomeRepository {
    suspend fun insert(income: Income): Long
    suspend fun update(income: Income)
    suspend fun delete(income: Income)
    fun getById(id: Long): Flow<Income?>
    fun getByCycleId(cycleId: Long): Flow<List<Income>>
    fun getFixedIncomes(cycleId: Long): Flow<List<Income>>
    fun getVariableIncomes(cycleId: Long): Flow<List<Income>>
}
