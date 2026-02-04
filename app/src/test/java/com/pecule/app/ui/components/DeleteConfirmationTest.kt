package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.ui.screens.dashboard.FakeExpenseRepository
import com.pecule.app.ui.screens.dashboard.FakeIncomeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DeleteConfirmationTest {

    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var incomeRepository: FakeIncomeRepository
    private lateinit var deleteHandler: DeleteHandler

    @Before
    fun setup() {
        expenseRepository = FakeExpenseRepository()
        incomeRepository = FakeIncomeRepository()
        deleteHandler = DeleteHandler(expenseRepository, incomeRepository)
    }

    @Test
    fun `deleteExpense calls expenseRepository delete`() = runTest {
        // Given: une dépense existante
        val expense = Expense(
            id = 1,
            cycleId = 1,
            category = Category.FOOD,
            label = "Courses",
            amount = 50.0,
            date = LocalDate.of(2025, 1, 15),
            isFixed = false
        )
        expenseRepository.setExpenses(listOf(expense))

        // When: on supprime la dépense
        deleteHandler.deleteExpense(expense)

        // Then: la dépense n'existe plus
        val remainingExpenses = expenseRepository.getByCycleId(1).first()
        assertTrue(remainingExpenses.isEmpty())
    }

    @Test
    fun `deleteIncome calls incomeRepository delete`() = runTest {
        // Given: un revenu existant
        val income = Income(
            id = 1,
            cycleId = 1,
            label = "Prime",
            amount = 200.0,
            date = LocalDate.of(2025, 1, 15),
            isFixed = false
        )
        incomeRepository.setIncomes(listOf(income))

        // When: on supprime le revenu
        deleteHandler.deleteIncome(income)

        // Then: le revenu n'existe plus
        val remainingIncomes = incomeRepository.getByCycleId(1).first()
        assertTrue(remainingIncomes.isEmpty())
    }

    @Test
    fun `after deleting expense the list is updated`() = runTest {
        // Given: plusieurs dépenses
        val expense1 = Expense(
            id = 1,
            cycleId = 1,
            category = Category.FOOD,
            label = "Courses",
            amount = 50.0,
            date = LocalDate.of(2025, 1, 15),
            isFixed = false
        )
        val expense2 = Expense(
            id = 2,
            cycleId = 1,
            category = Category.TRANSPORT,
            label = "Essence",
            amount = 60.0,
            date = LocalDate.of(2025, 1, 16),
            isFixed = false
        )
        expenseRepository.setExpenses(listOf(expense1, expense2))

        // When: on supprime une dépense
        deleteHandler.deleteExpense(expense1)

        // Then: seule la dépense restante est dans la liste
        val remainingExpenses = expenseRepository.getByCycleId(1).first()
        assertEquals(1, remainingExpenses.size)
        assertEquals(expense2.id, remainingExpenses.first().id)
    }

    @Test
    fun `after deleting income the list is updated`() = runTest {
        // Given: plusieurs revenus
        val income1 = Income(
            id = 1,
            cycleId = 1,
            label = "Prime",
            amount = 200.0,
            date = LocalDate.of(2025, 1, 15),
            isFixed = false
        )
        val income2 = Income(
            id = 2,
            cycleId = 1,
            label = "Vente",
            amount = 100.0,
            date = LocalDate.of(2025, 1, 16),
            isFixed = false
        )
        incomeRepository.setIncomes(listOf(income1, income2))

        // When: on supprime un revenu
        deleteHandler.deleteIncome(income1)

        // Then: seul le revenu restant est dans la liste
        val remainingIncomes = incomeRepository.getByCycleId(1).first()
        assertEquals(1, remainingIncomes.size)
        assertEquals(income2.id, remainingIncomes.first().id)
    }
}
