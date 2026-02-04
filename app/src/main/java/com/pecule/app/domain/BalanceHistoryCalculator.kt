package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import java.time.LocalDate

data class BalancePoint(
    val date: LocalDate,
    val balance: Double
)

class BalanceHistoryCalculator {

    fun calculate(
        cycle: BudgetCycle,
        expenses: List<Expense>,
        incomes: List<Income>
    ): List<BalancePoint> {
        val result = mutableListOf<BalancePoint>()

        // Start with initial balance on cycle start date
        var currentBalance = cycle.amount
        result.add(BalancePoint(cycle.startDate, currentBalance))

        // Group all transactions by date
        val transactionsByDate = mutableMapOf<LocalDate, Double>()

        // Add expenses (negative impact)
        expenses.forEach { expense ->
            val current = transactionsByDate.getOrDefault(expense.date, 0.0)
            transactionsByDate[expense.date] = current - expense.amount
        }

        // Add incomes (positive impact)
        incomes.forEach { income ->
            val current = transactionsByDate.getOrDefault(income.date, 0.0)
            transactionsByDate[income.date] = current + income.amount
        }

        // Sort dates and calculate cumulative balance
        transactionsByDate.keys
            .filter { it != cycle.startDate } // Don't duplicate start date
            .sorted()
            .forEach { date ->
                val dayDelta = transactionsByDate[date] ?: 0.0
                currentBalance += dayDelta
                result.add(BalancePoint(date, currentBalance))
            }

        return result
    }
}
