package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class BalanceCalculator @Inject constructor() {

    fun calculateBalance(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>
    ): Double {
        val totalExpenses = expenses.sumOf { it.amount }
        val totalIncomes = incomes.sumOf { it.amount }
        return cycle.amount + totalIncomes - totalExpenses
    }

    fun calculatePercentageUsed(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>
    ): Float {
        val totalBudget = cycle.amount + incomes.sumOf { it.amount }
        if (totalBudget <= 0) return 0f

        val totalExpenses = expenses.sumOf { it.amount }
        val percentage = (totalExpenses / totalBudget).toFloat()
        return min(percentage, 1.0f)
    }
}
