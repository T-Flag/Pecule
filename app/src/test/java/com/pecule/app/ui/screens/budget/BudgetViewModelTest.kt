package com.pecule.app.ui.screens.budget

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.ICategoryRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.domain.CategoryInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
class BudgetViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeBudgetCycleRepository: FakeBudgetCycleRepositoryForBudget
    private lateinit var fakeExpenseRepository: FakeExpenseRepositoryForBudget
    private lateinit var fakeIncomeRepository: FakeIncomeRepositoryForBudget
    private lateinit var fakeCategoryRepository: FakeCategoryRepositoryForBudget
    private lateinit var viewModel: BudgetViewModel

    // Category IDs from CategoryInitializer
    private val foodCategoryId = 2L
    private val transportCategoryId = 3L
    private val housingCategoryId = 4L
    private val utilitiesCategoryId = 5L
    private val entertainmentCategoryId = 6L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeBudgetCycleRepository = FakeBudgetCycleRepositoryForBudget()
        fakeExpenseRepository = FakeExpenseRepositoryForBudget()
        fakeIncomeRepository = FakeIncomeRepositoryForBudget()
        fakeCategoryRepository = FakeCategoryRepositoryForBudget()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): BudgetViewModel {
        return BudgetViewModel(
            budgetCycleRepository = fakeBudgetCycleRepository,
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            categoryRepository = fakeCategoryRepository
        )
    }

    // ==================== FIXED EXPENSES TESTS ====================

    @Test
    fun `fixedExpenses contient uniquement les depenses avec isFixed true du cycle actuel`() = runTest(testDispatcher) {
        // Given: un cycle avec des depenses fixes et variables
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, categoryId = housingCategoryId, label = "Loyer", amount = 800.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 2, cycleId = 1, categoryId = utilitiesCategoryId, label = "Internet", amount = 30.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 3, cycleId = 1, categoryId = foodCategoryId, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Expense(id = 4, cycleId = 2, categoryId = housingCategoryId, label = "Ancien loyer", amount = 750.0, date = LocalDate.of(2024, 12, 26), isFixed = true)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.fixedExpenses.collect {} }
        advanceUntilIdle()

        // Then
        val fixedExpenses = viewModel.fixedExpenses.value
        assertEquals(2, fixedExpenses.size)
        assertTrue(fixedExpenses.all { it.isFixed })
        assertTrue(fixedExpenses.all { it.cycleId == 1L })
        assertTrue(fixedExpenses.any { it.label == "Loyer" })
        assertTrue(fixedExpenses.any { it.label == "Internet" })

        job.cancel()
    }

    // ==================== VARIABLE EXPENSES TESTS ====================

    @Test
    fun `variableExpenses contient uniquement les depenses avec isFixed false du cycle actuel`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, categoryId = housingCategoryId, label = "Loyer", amount = 800.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 2, cycleId = 1, categoryId = foodCategoryId, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Expense(id = 3, cycleId = 1, categoryId = entertainmentCategoryId, label = "Cinema", amount = 15.0, date = LocalDate.of(2025, 1, 28), isFixed = false),
            Expense(id = 4, cycleId = 2, categoryId = foodCategoryId, label = "Ancien courses", amount = 45.0, date = LocalDate.of(2024, 12, 27), isFixed = false)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.variableExpenses.collect {} }
        advanceUntilIdle()

        // Then
        val variableExpenses = viewModel.variableExpenses.value
        assertEquals(2, variableExpenses.size)
        assertTrue(variableExpenses.all { !it.isFixed })
        assertTrue(variableExpenses.all { it.cycleId == 1L })
        assertTrue(variableExpenses.any { it.label == "Courses" })
        assertTrue(variableExpenses.any { it.label == "Cinema" })

        job.cancel()
    }

    // ==================== INCOMES TESTS ====================

    @Test
    fun `incomes contient tous les revenus du cycle actuel`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeIncomeRepository.setIncomes(listOf(
            Income(id = 1, cycleId = 1, label = "Pension", amount = 200.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Income(id = 2, cycleId = 1, label = "Vente Leboncoin", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Income(id = 3, cycleId = 1, label = "Loyer percu", amount = 400.0, date = LocalDate.of(2025, 1, 28), isFixed = true),
            Income(id = 4, cycleId = 2, label = "Ancien revenu", amount = 100.0, date = LocalDate.of(2024, 12, 26), isFixed = false)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.incomes.collect {} }
        advanceUntilIdle()

        // Then
        val incomes = viewModel.incomes.value
        assertEquals(3, incomes.size)
        assertTrue(incomes.all { it.cycleId == 1L })
        assertTrue(incomes.any { it.label == "Pension" })
        assertTrue(incomes.any { it.label == "Vente Leboncoin" })
        assertTrue(incomes.any { it.label == "Loyer percu" })

        job.cancel()
    }

    // ==================== TAB SELECTION TESTS ====================

    @Test
    fun `selectedTab initial est 0`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.selectedTab.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(0, viewModel.selectedTab.value)

        job.cancel()
    }

    @Test
    fun `selectTab met a jour selectedTab`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        viewModel = createViewModel()
        val job = launch { viewModel.selectedTab.collect {} }
        advanceUntilIdle()

        // When
        viewModel.selectTab(1)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.selectedTab.value)

        // When
        viewModel.selectTab(2)
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.selectedTab.value)

        job.cancel()
    }

    // ==================== TOTALS TESTS ====================

    @Test
    fun `totalFixed calcule correctement la somme des depenses fixes`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, categoryId = housingCategoryId, label = "Loyer", amount = 800.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 2, cycleId = 1, categoryId = utilitiesCategoryId, label = "Internet", amount = 30.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 3, cycleId = 1, categoryId = utilitiesCategoryId, label = "Electricite", amount = 70.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 4, cycleId = 1, categoryId = foodCategoryId, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.totalFixed.collect {} }
        advanceUntilIdle()

        // Then: 800 + 30 + 70 = 900
        assertEquals(900.0, viewModel.totalFixed.value, 0.001)

        job.cancel()
    }

    @Test
    fun `totalVariable calcule correctement la somme des depenses variables`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeExpenseRepository.setExpenses(listOf(
            Expense(id = 1, cycleId = 1, categoryId = housingCategoryId, label = "Loyer", amount = 800.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Expense(id = 2, cycleId = 1, categoryId = foodCategoryId, label = "Courses", amount = 85.50, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Expense(id = 3, cycleId = 1, categoryId = entertainmentCategoryId, label = "Cinema", amount = 14.50, date = LocalDate.of(2025, 1, 28), isFixed = false)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.totalVariable.collect {} }
        advanceUntilIdle()

        // Then: 85.50 + 14.50 = 100
        assertEquals(100.0, viewModel.totalVariable.value, 0.001)

        job.cancel()
    }

    @Test
    fun `totalIncomes calcule correctement la somme de tous les revenus`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        fakeIncomeRepository.setIncomes(listOf(
            Income(id = 1, cycleId = 1, label = "Pension", amount = 200.0, date = LocalDate.of(2025, 1, 26), isFixed = true),
            Income(id = 2, cycleId = 1, label = "Vente", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false),
            Income(id = 3, cycleId = 1, label = "Loyer percu", amount = 400.0, date = LocalDate.of(2025, 1, 28), isFixed = true)
        ))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.totalIncomes.collect {} }
        advanceUntilIdle()

        // Then: 200 + 50 + 400 = 650
        assertEquals(650.0, viewModel.totalIncomes.value, 0.001)

        job.cancel()
    }

    // ==================== DELETE TESTS ====================

    @Test
    fun `deleteExpense supprime la depense du repository`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val expenseToDelete = Expense(id = 1, cycleId = 1, categoryId = foodCategoryId, label = "Courses", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        fakeExpenseRepository.setExpenses(listOf(
            expenseToDelete,
            Expense(id = 2, cycleId = 1, categoryId = entertainmentCategoryId, label = "Cinema", amount = 15.0, date = LocalDate.of(2025, 1, 28), isFixed = false)
        ))

        viewModel = createViewModel()
        val job = launch { viewModel.variableExpenses.collect {} }
        advanceUntilIdle()

        // Verify we have 2 expenses initially
        assertEquals(2, viewModel.variableExpenses.value.size)

        // When
        viewModel.deleteExpense(expenseToDelete)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.variableExpenses.value.size)
        assertTrue(viewModel.variableExpenses.value.none { it.id == 1L })

        job.cancel()
    }

    @Test
    fun `deleteIncome supprime le revenu du repository`() = runTest(testDispatcher) {
        // Given
        fakeBudgetCycleRepository.setCurrentCycle(
            BudgetCycle(id = 1, amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null)
        )
        val incomeToDelete = Income(id = 1, cycleId = 1, label = "Pension", amount = 200.0, date = LocalDate.of(2025, 1, 26), isFixed = true)
        fakeIncomeRepository.setIncomes(listOf(
            incomeToDelete,
            Income(id = 2, cycleId = 1, label = "Vente", amount = 50.0, date = LocalDate.of(2025, 1, 27), isFixed = false)
        ))

        viewModel = createViewModel()
        val job = launch { viewModel.incomes.collect {} }
        advanceUntilIdle()

        // Verify we have 2 incomes initially
        assertEquals(2, viewModel.incomes.value.size)

        // When
        viewModel.deleteIncome(incomeToDelete)
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.incomes.value.size)
        assertTrue(viewModel.incomes.value.none { it.id == 1L })

        job.cancel()
    }
}

// ==================== FAKE REPOSITORIES ====================

class FakeBudgetCycleRepositoryForBudget : IBudgetCycleRepository {
    private val _currentCycle = MutableStateFlow<BudgetCycle?>(null)
    private val cycles = MutableStateFlow<List<BudgetCycle>>(emptyList())

    override val currentCycle: Flow<BudgetCycle?> = _currentCycle
    override val allCycles: Flow<List<BudgetCycle>> = cycles

    fun setCurrentCycle(cycle: BudgetCycle?) {
        _currentCycle.value = cycle
        if (cycle != null) {
            cycles.value = listOf(cycle)
        }
    }

    override suspend fun insert(cycle: BudgetCycle): Long {
        val id = (cycles.value.maxOfOrNull { it.id } ?: 0) + 1
        val newCycle = cycle.copy(id = id)
        cycles.value = cycles.value + newCycle
        if (newCycle.endDate == null) {
            _currentCycle.value = newCycle
        }
        return id
    }

    override suspend fun update(cycle: BudgetCycle) {
        cycles.value = cycles.value.map { if (it.id == cycle.id) cycle else it }
        if (cycle.id == _currentCycle.value?.id) {
            _currentCycle.value = cycle
        }
    }

    override suspend fun delete(cycle: BudgetCycle) {
        cycles.value = cycles.value.filter { it.id != cycle.id }
        if (_currentCycle.value?.id == cycle.id) {
            _currentCycle.value = null
        }
    }

    override fun getById(id: Long): Flow<BudgetCycle?> = cycles.map { list ->
        list.find { it.id == id }
    }
}

class FakeExpenseRepositoryForBudget : IExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())

    fun setExpenses(list: List<Expense>) {
        expenses.value = list
    }

    override suspend fun insert(expense: Expense): Long {
        val id = (expenses.value.maxOfOrNull { it.id } ?: 0) + 1
        val newExpense = expense.copy(id = id)
        expenses.value = expenses.value + newExpense
        return id
    }

    override suspend fun update(expense: Expense) {
        expenses.value = expenses.value.map { if (it.id == expense.id) expense else it }
    }

    override suspend fun delete(expense: Expense) {
        expenses.value = expenses.value.filter { it.id != expense.id }
    }

    override fun getById(id: Long): Flow<Expense?> = expenses.map { list ->
        list.find { it.id == id }
    }

    override fun getByCycleId(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId }
    }

    override fun getFixedExpenses(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId && it.isFixed }
    }

    override fun getVariableExpenses(cycleId: Long): Flow<List<Expense>> = expenses.map { list ->
        list.filter { it.cycleId == cycleId && !it.isFixed }
    }
}

class FakeIncomeRepositoryForBudget : IIncomeRepository {
    private val incomes = MutableStateFlow<List<Income>>(emptyList())

    fun setIncomes(list: List<Income>) {
        incomes.value = list
    }

    override suspend fun insert(income: Income): Long {
        val id = (incomes.value.maxOfOrNull { it.id } ?: 0) + 1
        val newIncome = income.copy(id = id)
        incomes.value = incomes.value + newIncome
        return id
    }

    override suspend fun update(income: Income) {
        incomes.value = incomes.value.map { if (it.id == income.id) income else it }
    }

    override suspend fun delete(income: Income) {
        incomes.value = incomes.value.filter { it.id != income.id }
    }

    override fun getById(id: Long): Flow<Income?> = incomes.map { list ->
        list.find { it.id == id }
    }

    override fun getByCycleId(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId }
    }

    override fun getFixedIncomes(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId && it.isFixed }
    }

    override fun getVariableIncomes(cycleId: Long): Flow<List<Income>> = incomes.map { list ->
        list.filter { it.cycleId == cycleId && !it.isFixed }
    }
}

class FakeCategoryRepositoryForBudget : ICategoryRepository {
    private val categories = MutableStateFlow(CategoryInitializer.DEFAULT_CATEGORIES)

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categories

    override fun getDefaultCategories(): Flow<List<CategoryEntity>> = categories.map { list ->
        list.filter { it.isDefault }
    }

    override fun getById(id: Long): Flow<CategoryEntity?> = categories.map { list ->
        list.find { it.id == id }
    }

    override suspend fun insert(category: CategoryEntity): Long = category.id

    override suspend fun insertAll(categories: List<CategoryEntity>) {}

    override suspend fun update(category: CategoryEntity) {}

    override suspend fun delete(category: CategoryEntity) {}

    override suspend fun getCount(): Int = categories.value.size
}
