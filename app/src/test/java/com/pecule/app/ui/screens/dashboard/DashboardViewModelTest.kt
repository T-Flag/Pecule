package com.pecule.app.ui.screens.dashboard

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Category
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.domain.BalanceCalculator
import com.pecule.app.ui.screens.onboarding.FakeBudgetCycleRepository
import com.pecule.app.ui.screens.onboarding.FakeUserPreferencesRepository
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
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeBudgetCycleRepository: FakeBudgetCycleRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeIncomeRepository: FakeIncomeRepository
    private lateinit var balanceCalculator: BalanceCalculator
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        fakeBudgetCycleRepository = FakeBudgetCycleRepository()
        fakeExpenseRepository = FakeExpenseRepository()
        fakeIncomeRepository = FakeIncomeRepository()
        balanceCalculator = BalanceCalculator()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            userPreferencesRepository = fakeUserPreferencesRepository,
            budgetCycleRepository = fakeBudgetCycleRepository,
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            balanceCalculator = balanceCalculator
        )
    }

    @Test
    fun `userName comes from UserPreferencesRepository`() = runTest(testDispatcher) {
        // Given: user with name "Jean"
        fakeUserPreferencesRepository.updateFirstName("Jean")

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.userName.collect {} }
        advanceUntilIdle()

        // Then: userName should be "Jean"
        assertEquals("Jean", viewModel.userName.value)
        job.cancel()
    }

    @Test
    fun `currentBalance is calculated correctly from current cycle`() = runTest(testDispatcher) {
        // Given: cycle with 2500.0, expenses 300.0, incomes 100.0
        fakeBudgetCycleRepository.insert(
            BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 300.0, date = LocalDate.of(2025, 1, 26))
        ))
        fakeIncomeRepository.setIncomes(listOf(
            Income(id = 1, cycleId = 1, label = "Prime", amount = 100.0, date = LocalDate.of(2025, 1, 26))
        ))

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.currentBalance.collect {} }
        advanceUntilIdle()

        // Then: balance = 2500 + 100 - 300 = 2300
        assertEquals(2300.0, viewModel.currentBalance.value, 0.001)
        job.cancel()
    }

    @Test
    fun `budgetPercentageUsed is between 0 and 1`() = runTest(testDispatcher) {
        // Given: cycle with 2000.0, expenses 500.0 (25% used)
        fakeBudgetCycleRepository.insert(
            BudgetCycle(amount = 2000.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 500.0, date = LocalDate.of(2025, 1, 26))
        ))
        fakeIncomeRepository.setIncomes(emptyList())

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.budgetPercentageUsed.collect {} }
        advanceUntilIdle()

        // Then: percentage should be 0.25 and within [0, 1]
        val percentage = viewModel.budgetPercentageUsed.value
        assertTrue(percentage >= 0.0f)
        assertTrue(percentage <= 1.0f)
        assertEquals(0.25f, percentage, 0.001f)
        job.cancel()
    }

    @Test
    fun `recentTransactions contains max 5 elements`() = runTest(testDispatcher) {
        // Given: 7 variable expenses
        fakeBudgetCycleRepository.insert(
            BudgetCycle(amount = 3000.0, startDate = LocalDate.of(2025, 1, 20), endDate = null)
        )
        val manyExpenses = (1..7).map { i ->
            Expense(
                id = i.toLong(),
                cycleId = 1,
                category = Category.FOOD,
                label = "Expense $i",
                amount = 100.0,
                date = LocalDate.of(2025, 1, 20 + i),
                isFixed = false
            )
        }
        fakeExpenseRepository.setExpenses(manyExpenses)
        fakeIncomeRepository.setIncomes(emptyList())

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.recentTransactions.collect {} }
        advanceUntilIdle()

        // Then: recentTransactions should have max 5 elements
        val transactions = viewModel.recentTransactions.value
        assertTrue(transactions.size <= 5)
        job.cancel()
    }

    @Test
    fun `recentTransactions excludes fixed transactions`() = runTest(testDispatcher) {
        // Given: mix of fixed and variable expenses
        fakeBudgetCycleRepository.insert(
            BudgetCycle(amount = 3000.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, category = Category.HOUSING, label = "Loyer", amount = 800.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 2, cycleId = 1, category = Category.FOOD, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Expense(id = 3, cycleId = 1, category = Category.UTILITIES, label = "Internet", amount = 30.0, date = LocalDate.of(2025, 1, 26), isFixed = true)
        ))
        fakeIncomeRepository.setIncomes(listOf(
            Income(id = 1, cycleId = 1, label = "Pension", amount = 200.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Income(id = 2, cycleId = 1, label = "Vente", amount = 50.0, date = LocalDate.of(2025, 1, 28), isFixed = false)
        ))

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.recentTransactions.collect {} }
        advanceUntilIdle()

        // Then: only variable transactions (isFixed = false)
        val transactions = viewModel.recentTransactions.value
        assertTrue(transactions.all { !it.isFixed })
        assertEquals(2, transactions.size) // 1 variable expense + 1 variable income
        job.cancel()
    }

    @Test
    fun `recentTransactions sorted by date descending`() = runTest(testDispatcher) {
        // Given: expenses with different dates
        fakeBudgetCycleRepository.insert(
            BudgetCycle(amount = 3000.0, startDate = LocalDate.of(2025, 1, 20), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, category = Category.FOOD, label = "Old", amount = 10.0, date = LocalDate.of(2025, 1, 21), isFixed = false),
            Expense(id = 2, cycleId = 1, category = Category.FOOD, label = "New", amount = 20.0, date = LocalDate.of(2025, 1, 25), isFixed = false),
            Expense(id = 3, cycleId = 1, category = Category.FOOD, label = "Middle", amount = 30.0, date = LocalDate.of(2025, 1, 23), isFixed = false)
        ))
        fakeIncomeRepository.setIncomes(emptyList())

        // When: creating ViewModel
        viewModel = createViewModel()

        // Collect to trigger WhileSubscribed
        val job = launch { viewModel.recentTransactions.collect {} }
        advanceUntilIdle()

        // Then: sorted by date descending (newest first)
        val transactions = viewModel.recentTransactions.value
        assertEquals(3, transactions.size)
        assertEquals("New", transactions[0].label)
        assertEquals("Middle", transactions[1].label)
        assertEquals("Old", transactions[2].label)
        job.cancel()
    }
}
