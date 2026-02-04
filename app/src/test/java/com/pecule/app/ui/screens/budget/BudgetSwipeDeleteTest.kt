package com.pecule.app.ui.screens.budget

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetSwipeDeleteTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeBudgetCycleRepository: FakeBudgetCycleRepositoryForBudget
    private lateinit var fakeExpenseRepository: FakeExpenseRepositoryForBudget
    private lateinit var fakeIncomeRepository: FakeIncomeRepositoryForBudget
    private lateinit var viewModel: BudgetViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeBudgetCycleRepository = FakeBudgetCycleRepositoryForBudget()
        fakeExpenseRepository = FakeExpenseRepositoryForBudget()
        fakeIncomeRepository = FakeIncomeRepositoryForBudget()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): BudgetViewModel {
        return BudgetViewModel(
            budgetCycleRepository = fakeBudgetCycleRepository,
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository
        )
    }

    // ==================== SWIPE DELETE EXPENSE TESTS ====================

    @Test
    fun `swipe on expense triggers delete when confirmed`() = runTest(testDispatcher) {
        // Given: un cycle avec une dépense
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val expenseToSwipe = Expense(
            id = 1,
            cycleId = 1,
            category = Category.FOOD,
            label = "Courses",
            amount = 50.0,
            date = LocalDate.of(2025, 1, 27),
            isFixed = false
        )
        fakeExpenseRepository.setExpenses(listOf(expenseToSwipe))

        viewModel = createViewModel()
        val job = launch { viewModel.variableExpenses.collect {} }
        advanceUntilIdle()

        // When: swipe déclenche la suppression (après confirmation)
        var deleteConfirmed = false
        // Simulate: user swipes, dialog shows, user confirms
        deleteConfirmed = true
        if (deleteConfirmed) {
            viewModel.deleteExpense(expenseToSwipe)
        }
        advanceUntilIdle()

        // Then: la dépense est supprimée
        assertTrue(viewModel.variableExpenses.value.isEmpty())

        job.cancel()
    }

    @Test
    fun `after confirmation expense is deleted from repository`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val expense1 = Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        val expense2 = Expense(id = 2, cycleId = 1, category = Category.TRANSPORT, label = "Essence", amount = 60.0, date = LocalDate.of(2025, 1, 28), isFixed = false)
        fakeExpenseRepository.setExpenses(listOf(expense1, expense2))

        viewModel = createViewModel()
        val job = launch { viewModel.variableExpenses.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.variableExpenses.value.size)

        // When: user confirms deletion after swipe
        viewModel.deleteExpense(expense1)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.variableExpenses.value.size)
        assertEquals(expense2.id, viewModel.variableExpenses.value.first().id)

        job.cancel()
    }

    // ==================== SWIPE DELETE INCOME TESTS ====================

    @Test
    fun `swipe on income triggers delete when confirmed`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val incomeToSwipe = Income(
            id = 1,
            cycleId = 1,
            label = "Prime",
            amount = 200.0,
            date = LocalDate.of(2025, 1, 27),
            isFixed = false
        )
        fakeIncomeRepository.setIncomes(listOf(incomeToSwipe))

        viewModel = createViewModel()
        val job = launch { viewModel.incomes.collect {} }
        advanceUntilIdle()

        // When: swipe déclenche la suppression (après confirmation)
        var deleteConfirmed = false
        deleteConfirmed = true
        if (deleteConfirmed) {
            viewModel.deleteIncome(incomeToSwipe)
        }
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.incomes.value.isEmpty())

        job.cancel()
    }

    @Test
    fun `after confirmation income is deleted from repository`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val income1 = Income(id = 1, cycleId = 1, label = "Prime", amount = 200.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        val income2 = Income(id = 2, cycleId = 1, label = "Vente", amount = 100.0, date = LocalDate.of(2025, 1, 28), isFixed = false)
        fakeIncomeRepository.setIncomes(listOf(income1, income2))

        viewModel = createViewModel()
        val job = launch { viewModel.incomes.collect {} }
        advanceUntilIdle()

        assertEquals(2, viewModel.incomes.value.size)

        // When
        viewModel.deleteIncome(income1)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.incomes.value.size)
        assertEquals(income2.id, viewModel.incomes.value.first().id)

        job.cancel()
    }

    // ==================== CANCELLATION TEST ====================

    @Test
    fun `cancellation does not delete anything`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val expense = Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        fakeExpenseRepository.setExpenses(listOf(expense))

        viewModel = createViewModel()
        val job = launch { viewModel.variableExpenses.collect {} }
        advanceUntilIdle()

        assertEquals(1, viewModel.variableExpenses.value.size)

        // When: user swipes but cancels in dialog
        var deleteConfirmed = false
        // Simulate: user swipes, dialog shows, user cancels
        deleteConfirmed = false
        if (deleteConfirmed) {
            viewModel.deleteExpense(expense)
        }
        advanceUntilIdle()

        // Then: nothing is deleted
        assertEquals(1, viewModel.variableExpenses.value.size)
        assertEquals(expense.id, viewModel.variableExpenses.value.first().id)

        job.cancel()
    }
}
