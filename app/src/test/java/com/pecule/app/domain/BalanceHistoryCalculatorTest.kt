package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BalanceHistoryCalculatorTest {

    private val calculator = BalanceHistoryCalculator()

    private val testCycle = BudgetCycle(
        id = 1,
        amount = 2500.0,
        startDate = LocalDate.of(2025, 1, 25),
        endDate = LocalDate.of(2025, 2, 24)
    )

    @Test
    fun `cycle without transactions returns single entry with initial amount`() {
        // Given
        val expenses = emptyList<Expense>()
        val incomes = emptyList<Income>()

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then
        assertEquals(1, history.size)
        assertEquals(testCycle.startDate, history[0].date)
        assertEquals(2500.0, history[0].balance, 0.01)
    }

    @Test
    fun `cycle with transactions shows balance evolution day by day`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 100.0, LocalDate.of(2025, 1, 26), false),
            Expense(2, 1, 3, "Essence", 50.0, LocalDate.of(2025, 1, 28), false)
        )
        val incomes = listOf(
            Income(1, 1, "Prime", 200.0, LocalDate.of(2025, 1, 27), false)
        )

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then
        assertEquals(4, history.size) // initial + 3 transaction days

        // Day 0 (start): 2500
        assertEquals(LocalDate.of(2025, 1, 25), history[0].date)
        assertEquals(2500.0, history[0].balance, 0.01)

        // Day 1: 2500 - 100 = 2400
        assertEquals(LocalDate.of(2025, 1, 26), history[1].date)
        assertEquals(2400.0, history[1].balance, 0.01)

        // Day 2: 2400 + 200 = 2600
        assertEquals(LocalDate.of(2025, 1, 27), history[2].date)
        assertEquals(2600.0, history[2].balance, 0.01)

        // Day 3: 2600 - 50 = 2550
        assertEquals(LocalDate.of(2025, 1, 28), history[3].date)
        assertEquals(2550.0, history[3].balance, 0.01)
    }

    @Test
    fun `transactions on same day are cumulated`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 100.0, LocalDate.of(2025, 1, 26), false),
            Expense(2, 1, 3, "Restaurant", 50.0, LocalDate.of(2025, 1, 26), false)
        )
        val incomes = listOf(
            Income(1, 1, "Remboursement", 30.0, LocalDate.of(2025, 1, 26), false)
        )

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then
        assertEquals(2, history.size) // initial + 1 day with all transactions

        // Day 0 (start): 2500
        assertEquals(2500.0, history[0].balance, 0.01)

        // Day 1: 2500 - 100 - 50 + 30 = 2380
        assertEquals(LocalDate.of(2025, 1, 26), history[1].date)
        assertEquals(2380.0, history[1].balance, 0.01)
    }

    @Test
    fun `balance is calculated chronologically`() {
        // Given: transactions not in chronological order
        val expenses = listOf(
            Expense(1, 1, 2, "Later expense", 200.0, LocalDate.of(2025, 1, 30), false),
            Expense(2, 1, 3, "Earlier expense", 100.0, LocalDate.of(2025, 1, 26), false)
        )
        val incomes = emptyList<Income>()

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then: should be sorted chronologically
        assertEquals(3, history.size)

        assertEquals(LocalDate.of(2025, 1, 25), history[0].date)
        assertEquals(2500.0, history[0].balance, 0.01)

        assertEquals(LocalDate.of(2025, 1, 26), history[1].date)
        assertEquals(2400.0, history[1].balance, 0.01) // 2500 - 100

        assertEquals(LocalDate.of(2025, 1, 30), history[2].date)
        assertEquals(2200.0, history[2].balance, 0.01) // 2400 - 200
    }

    @Test
    fun `days without transactions do not create points`() {
        // Given: transactions with gaps between days
        val expenses = listOf(
            Expense(1, 1, 2, "Day 1", 100.0, LocalDate.of(2025, 1, 26), false),
            Expense(2, 1, 3, "Day 5", 50.0, LocalDate.of(2025, 1, 30), false)
        )
        val incomes = emptyList<Income>()

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then: only 3 points (initial, day 1, day 5) - no points for days 2, 3, 4
        assertEquals(3, history.size)
        assertEquals(LocalDate.of(2025, 1, 25), history[0].date)
        assertEquals(LocalDate.of(2025, 1, 26), history[1].date)
        assertEquals(LocalDate.of(2025, 1, 30), history[2].date)
    }

    @Test
    fun `balance can go negative`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Big expense", 3000.0, LocalDate.of(2025, 1, 26), false)
        )
        val incomes = emptyList<Income>()

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then
        assertEquals(2, history.size)
        assertEquals(2500.0, history[0].balance, 0.01)
        assertEquals(-500.0, history[1].balance, 0.01)
    }

    @Test
    fun `only incomes increase balance`() {
        // Given
        val expenses = emptyList<Expense>()
        val incomes = listOf(
            Income(1, 1, "Bonus 1", 500.0, LocalDate.of(2025, 1, 26), false),
            Income(2, 1, "Bonus 2", 300.0, LocalDate.of(2025, 1, 27), false)
        )

        // When
        val history = calculator.calculate(testCycle, expenses, incomes)

        // Then
        assertEquals(3, history.size)
        assertEquals(2500.0, history[0].balance, 0.01)
        assertEquals(3000.0, history[1].balance, 0.01) // 2500 + 500
        assertEquals(3300.0, history[2].balance, 0.01) // 3000 + 300
    }
}
