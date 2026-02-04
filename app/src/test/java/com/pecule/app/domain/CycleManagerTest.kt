package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.ui.screens.dashboard.FakeExpenseRepository
import com.pecule.app.ui.screens.dashboard.FakeIncomeRepository
import com.pecule.app.ui.screens.onboarding.FakeBudgetCycleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CycleManagerTest {

    private lateinit var cycleRepository: FakeBudgetCycleRepository
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var incomeRepository: FakeIncomeRepository
    private lateinit var cycleManager: CycleManager

    // Category IDs from CategoryInitializer
    private val foodCategoryId = 2L
    private val housingCategoryId = 4L
    private val utilitiesCategoryId = 5L

    @Before
    fun setup() {
        cycleRepository = FakeBudgetCycleRepository()
        expenseRepository = FakeExpenseRepository()
        incomeRepository = FakeIncomeRepository()
        cycleManager = CycleManager(
            budgetCycleRepository = cycleRepository,
            expenseRepository = expenseRepository,
            incomeRepository = incomeRepository
        )
    }

    @Test
    fun `createNewCycle closes current cycle by setting endDate to day before new date`() = runTest {
        // Given: un cycle actuel ouvert
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: le cycle precedent est ferme avec endDate = veille du nouveau cycle
        val closedCycle = cycleRepository.getById(currentCycleId).first()
        assertEquals(LocalDate.of(2025, 2, 14), closedCycle?.endDate)
    }

    @Test
    fun `createNewCycle creates new cycle with provided amount and date`() = runTest {
        // Given: un cycle actuel
        cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )

        // When: on cree un nouveau cycle
        val newAmount = 2500.0
        val newDate = LocalDate.of(2025, 2, 15)
        cycleManager.createNewCycle(amount = newAmount, startDate = newDate)

        // Then: le nouveau cycle existe avec les bonnes valeurs
        val newCycle = cycleRepository.currentCycle.first()
        assertEquals(newAmount, newCycle?.amount)
        assertEquals(newDate, newCycle?.startDate)
        assertNull(newCycle?.endDate)
    }

    @Test
    fun `createNewCycle returns the ID of the new cycle`() = runTest {
        // Given: un cycle actuel
        cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )

        // When: on cree un nouveau cycle
        val newCycleId = cycleManager.createNewCycle(
            amount = 2200.0,
            startDate = LocalDate.of(2025, 2, 15)
        )

        // Then: l'ID retourne correspond au nouveau cycle
        val newCycle = cycleRepository.getById(newCycleId).first()
        assertEquals(2200.0, newCycle?.amount)
    }

    @Test
    fun `createNewCycle duplicates all fixed expenses from previous cycle`() = runTest {
        // Given: un cycle avec des depenses fixes et variables
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = housingCategoryId,
                label = "Loyer",
                amount = 800.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = utilitiesCategoryId,
                label = "Internet",
                amount = 40.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = foodCategoryId,
                label = "Courses",
                amount = 50.0,
                date = LocalDate.of(2025, 1, 20),
                isFixed = false
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: les 2 depenses fixes sont dupliquees dans le nouveau cycle
        val newCycleExpenses = expenseRepository.getByCycleId(newCycleId).first()
        assertEquals(2, newCycleExpenses.size)
    }

    @Test
    fun `duplicated expenses have new cycleId but keep same label, amount, categoryId, isFixed`() = runTest {
        // Given: un cycle avec une depense fixe
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = housingCategoryId,
                label = "Loyer appartement",
                amount = 850.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )

        // When: on cree un nouveau cycle
        val newCycleId = cycleManager.createNewCycle(
            amount = 2200.0,
            startDate = LocalDate.of(2025, 2, 15)
        )

        // Then: la depense dupliquee garde les memes proprietes
        val duplicatedExpense = expenseRepository.getByCycleId(newCycleId).first().first()
        assertEquals(newCycleId, duplicatedExpense.cycleId)
        assertEquals("Loyer appartement", duplicatedExpense.label)
        assertEquals(850.0, duplicatedExpense.amount, 0.01)
        assertEquals(housingCategoryId, duplicatedExpense.categoryId)
        assertEquals(true, duplicatedExpense.isFixed)
    }

    @Test
    fun `duplicated expenses have new date matching new cycle start date`() = runTest {
        // Given: un cycle avec une depense fixe
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = housingCategoryId,
                label = "Loyer",
                amount = 800.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: la depense dupliquee a la date du nouveau cycle
        val duplicatedExpense = expenseRepository.getByCycleId(newCycleId).first().first()
        assertEquals(newDate, duplicatedExpense.date)
    }

    @Test
    fun `createNewCycle duplicates all fixed incomes from previous cycle`() = runTest {
        // Given: un cycle avec des revenus fixes et variables
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        incomeRepository.insert(
            Income(
                cycleId = currentCycleId,
                label = "Pension",
                amount = 300.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )
        incomeRepository.insert(
            Income(
                cycleId = currentCycleId,
                label = "Loyer percu",
                amount = 500.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )
        incomeRepository.insert(
            Income(
                cycleId = currentCycleId,
                label = "Prime exceptionnelle",
                amount = 200.0,
                date = LocalDate.of(2025, 1, 20),
                isFixed = false
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: les 2 revenus fixes sont dupliques dans le nouveau cycle
        val newCycleIncomes = incomeRepository.getByCycleId(newCycleId).first()
        assertEquals(2, newCycleIncomes.size)
    }

    @Test
    fun `duplicated incomes have new cycleId but keep same label, amount, isFixed`() = runTest {
        // Given: un cycle avec un revenu fixe
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        incomeRepository.insert(
            Income(
                cycleId = currentCycleId,
                label = "Pension alimentaire",
                amount = 350.0,
                date = LocalDate.of(2025, 1, 15),
                isFixed = true
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: le revenu duplique garde les memes proprietes avec la nouvelle date
        val duplicatedIncome = incomeRepository.getByCycleId(newCycleId).first().first()
        assertEquals(newCycleId, duplicatedIncome.cycleId)
        assertEquals("Pension alimentaire", duplicatedIncome.label)
        assertEquals(350.0, duplicatedIncome.amount, 0.01)
        assertEquals(true, duplicatedIncome.isFixed)
        assertEquals(newDate, duplicatedIncome.date)
    }

    @Test
    fun `createNewCycle without current cycle simply creates new cycle`() = runTest {
        // Given: aucun cycle existant

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 1, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2000.0, startDate = newDate)

        // Then: le cycle est cree correctement
        val allCycles = cycleRepository.allCycles.first()
        assertEquals(1, allCycles.size)

        val newCycle = cycleRepository.getById(newCycleId).first()
        assertEquals(2000.0, newCycle?.amount)
        assertEquals(newDate, newCycle?.startDate)
        assertNull(newCycle?.endDate)
    }

    @Test
    fun `createNewCycle with current cycle but no fixed items duplicates nothing`() = runTest {
        // Given: un cycle avec uniquement des depenses et revenus variables
        val currentCycleId = cycleRepository.insert(
            BudgetCycle(
                amount = 2000.0,
                startDate = LocalDate.of(2025, 1, 15),
                endDate = null
            )
        )
        expenseRepository.insert(
            Expense(
                cycleId = currentCycleId,
                categoryId = foodCategoryId,
                label = "Restaurant",
                amount = 45.0,
                date = LocalDate.of(2025, 1, 20),
                isFixed = false
            )
        )
        incomeRepository.insert(
            Income(
                cycleId = currentCycleId,
                label = "Vente Leboncoin",
                amount = 80.0,
                date = LocalDate.of(2025, 1, 22),
                isFixed = false
            )
        )

        // When: on cree un nouveau cycle
        val newDate = LocalDate.of(2025, 2, 15)
        val newCycleId = cycleManager.createNewCycle(amount = 2200.0, startDate = newDate)

        // Then: aucune depense ni revenu n'est duplique
        val newCycleExpenses = expenseRepository.getByCycleId(newCycleId).first()
        val newCycleIncomes = incomeRepository.getByCycleId(newCycleId).first()
        assertEquals(0, newCycleExpenses.size)
        assertEquals(0, newCycleIncomes.size)
    }
}
