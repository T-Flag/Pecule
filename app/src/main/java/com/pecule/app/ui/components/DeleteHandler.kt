package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeleteHandler @Inject constructor(
    private val expenseRepository: IExpenseRepository,
    private val incomeRepository: IIncomeRepository
) {
    suspend fun deleteExpense(expense: Expense) {
        expenseRepository.delete(expense)
    }

    suspend fun deleteIncome(income: Income) {
        incomeRepository.delete(income)
    }
}
