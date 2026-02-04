package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleManager @Inject constructor(
    private val budgetCycleRepository: IBudgetCycleRepository,
    private val expenseRepository: IExpenseRepository,
    private val incomeRepository: IIncomeRepository
) {
    suspend fun createNewCycle(amount: Double, startDate: LocalDate): Long {
        val currentCycle = budgetCycleRepository.currentCycle.first()

        if (currentCycle != null) {
            val closedCycle = currentCycle.copy(endDate = startDate.minusDays(1))
            budgetCycleRepository.update(closedCycle)
        }

        val newCycle = BudgetCycle(
            amount = amount,
            startDate = startDate,
            endDate = null
        )
        val newCycleId = budgetCycleRepository.insert(newCycle)

        if (currentCycle != null) {
            duplicateFixedExpenses(currentCycle.id, newCycleId, startDate)
            duplicateFixedIncomes(currentCycle.id, newCycleId, startDate)
        }

        return newCycleId
    }

    private suspend fun duplicateFixedExpenses(
        oldCycleId: Long,
        newCycleId: Long,
        newDate: LocalDate
    ) {
        val fixedExpenses = expenseRepository.getFixedExpenses(oldCycleId).first()
        fixedExpenses.forEach { expense ->
            val duplicatedExpense = Expense(
                id = 0,
                cycleId = newCycleId,
                categoryId = expense.categoryId,
                label = expense.label,
                amount = expense.amount,
                date = newDate,
                isFixed = expense.isFixed
            )
            expenseRepository.insert(duplicatedExpense)
        }
    }

    private suspend fun duplicateFixedIncomes(
        oldCycleId: Long,
        newCycleId: Long,
        newDate: LocalDate
    ) {
        val fixedIncomes = incomeRepository.getFixedIncomes(oldCycleId).first()
        fixedIncomes.forEach { income ->
            val duplicatedIncome = Income(
                id = 0,
                cycleId = newCycleId,
                label = income.label,
                amount = income.amount,
                date = newDate,
                isFixed = income.isFixed
            )
            incomeRepository.insert(duplicatedIncome)
        }
    }
}
