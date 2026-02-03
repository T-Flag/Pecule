package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class BalanceCalculatorTest {

    private lateinit var calculator: BalanceCalculator

    @Before
    fun setup() {
        calculator = BalanceCalculator()
    }

    @Test
    fun `calculateBalance with cycle only and no transactions returns cycle amount`() {
        // Given: cycle with amount 2500.0, no expenses, no incomes
        val cycle = BudgetCycle(
            id = 1,
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = emptyList<Expense>()
        val incomes = emptyList<Income>()

        // When: calculating balance
        val balance = calculator.calculateBalance(cycle, expenses, incomes)

        // Then: balance should equal cycle amount
        assertEquals(2500.0, balance, 0.001)
    }

    @Test
    fun `calculateBalance with expenses only subtracts from cycle amount`() {
        // Given: cycle 2500.0, expenses [100.0, 50.0, 200.0]
        val cycle = BudgetCycle(
            id = 1,
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = listOf(
            Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 100.0, date = LocalDate.of(2025, 1, 26)),
            Expense(id = 2, cycleId = 1, category = Category.TRANSPORT, label = "Essence", amount = 50.0, date = LocalDate.of(2025, 1, 27)),
            Expense(id = 3, cycleId = 1, category = Category.ENTERTAINMENT, label = "Cinéma", amount = 200.0, date = LocalDate.of(2025, 1, 28))
        )
        val incomes = emptyList<Income>()

        // When: calculating balance
        val balance = calculator.calculateBalance(cycle, expenses, incomes)

        // Then: balance = 2500 - (100 + 50 + 200) = 2150
        assertEquals(2150.0, balance, 0.001)
    }

    @Test
    fun `calculateBalance with incomes only adds to cycle amount`() {
        // Given: cycle 2500.0, incomes [100.0, 50.0]
        val cycle = BudgetCycle(
            id = 1,
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = emptyList<Expense>()
        val incomes = listOf(
            Income(id = 1, cycleId = 1, label = "Remboursement", amount = 100.0, date = LocalDate.of(2025, 1, 26)),
            Income(id = 2, cycleId = 1, label = "Prime", amount = 50.0, date = LocalDate.of(2025, 1, 27))
        )

        // When: calculating balance
        val balance = calculator.calculateBalance(cycle, expenses, incomes)

        // Then: balance = 2500 + (100 + 50) = 2650
        assertEquals(2650.0, balance, 0.001)
    }

    @Test
    fun `calculateBalance with expenses and incomes applies both`() {
        // Given: cycle 2500.0, expenses [500.0], incomes [200.0]
        val cycle = BudgetCycle(
            id = 1,
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = listOf(
            Expense(id = 1, cycleId = 1, category = Category.SHOPPING, label = "Vêtements", amount = 500.0, date = LocalDate.of(2025, 1, 26))
        )
        val incomes = listOf(
            Income(id = 1, cycleId = 1, label = "Vente", amount = 200.0, date = LocalDate.of(2025, 1, 27))
        )

        // When: calculating balance
        val balance = calculator.calculateBalance(cycle, expenses, incomes)

        // Then: balance = 2500 + 200 - 500 = 2200
        assertEquals(2200.0, balance, 0.001)
    }

    @Test
    fun `calculatePercentageUsed returns correct percentage`() {
        // Given: cycle 2000.0, incomes 500.0 (total budget = 2500), expenses 1000.0
        val cycle = BudgetCycle(
            id = 1,
            amount = 2000.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = listOf(
            Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 1000.0, date = LocalDate.of(2025, 1, 26))
        )
        val incomes = listOf(
            Income(id = 1, cycleId = 1, label = "Prime", amount = 500.0, date = LocalDate.of(2025, 1, 26))
        )

        // When: calculating percentage used
        val percentage = calculator.calculatePercentageUsed(cycle, expenses, incomes)

        // Then: percentage = 1000 / 2500 = 0.4 (40%)
        assertEquals(0.4f, percentage, 0.001f)
    }

    @Test
    fun `calculatePercentageUsed caps at 100 percent when overspent`() {
        // Given: cycle 1000.0, no incomes, expenses 1500.0 (overspent)
        val cycle = BudgetCycle(
            id = 1,
            amount = 1000.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        val expenses = listOf(
            Expense(id = 1, cycleId = 1, category = Category.SHOPPING, label = "Achat", amount = 1500.0, date = LocalDate.of(2025, 1, 26))
        )
        val incomes = emptyList<Income>()

        // When: calculating percentage used
        val percentage = calculator.calculatePercentageUsed(cycle, expenses, incomes)

        // Then: percentage should be capped at 1.0 (100%)
        assertEquals(1.0f, percentage, 0.001f)
    }
}
