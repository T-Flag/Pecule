package com.pecule.app.ui.screens.statistics

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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeBudgetCycleRepository: FakeBudgetCycleRepositoryForStats
    private lateinit var fakeExpenseRepository: FakeExpenseRepositoryForStats
    private lateinit var fakeIncomeRepository: FakeIncomeRepositoryForStats
    private lateinit var fakeCategoryRepository: FakeCategoryRepositoryForStats
    private lateinit var viewModel: StatisticsViewModel

    // Category IDs from CategoryInitializer
    private val foodCategoryId = 2L
    private val transportCategoryId = 3L
    private val housingCategoryId = 4L

    // Category entities for assertions
    private val foodCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.id == foodCategoryId }!!
    private val transportCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.id == transportCategoryId }!!
    private val housingCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.id == housingCategoryId }!!

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeBudgetCycleRepository = FakeBudgetCycleRepositoryForStats()
        fakeExpenseRepository = FakeExpenseRepositoryForStats()
        fakeIncomeRepository = FakeIncomeRepositoryForStats()
        fakeCategoryRepository = FakeCategoryRepositoryForStats()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): StatisticsViewModel {
        return StatisticsViewModel(
            fakeBudgetCycleRepository,
            fakeExpenseRepository,
            fakeIncomeRepository,
            fakeCategoryRepository
        )
    }

    @Test
    fun `cycles contains all cycles from repository`() = runTest(testDispatcher) {
        // Given
        val cycle1 = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        val cycle2 = BudgetCycle(2, 2500.0, LocalDate.of(2025, 2, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle1, cycle2))

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.cycles.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.cycles.value.size)
        assertEquals(cycle1, viewModel.cycles.value[0])
        assertEquals(cycle2, viewModel.cycles.value[1])
        job.cancel()
    }

    @Test
    fun `selectedCycle is initialized with current cycle (endDate = null)`() = runTest(testDispatcher) {
        // Given
        val closedCycle = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        val currentCycle = BudgetCycle(2, 2500.0, LocalDate.of(2025, 2, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(closedCycle, currentCycle))
        fakeBudgetCycleRepository.setCurrentCycle(currentCycle)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.selectedCycle.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(currentCycle, viewModel.selectedCycle.value)
        job.cancel()
    }

    @Test
    fun `selectCycle updates selectedCycle`() = runTest(testDispatcher) {
        // Given
        val cycle1 = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        val cycle2 = BudgetCycle(2, 2500.0, LocalDate.of(2025, 2, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle1, cycle2))
        fakeBudgetCycleRepository.setCurrentCycle(cycle2)

        viewModel = createViewModel()
        val job = launch { viewModel.selectedCycle.collect {} }
        advanceUntilIdle()
        assertEquals(cycle2, viewModel.selectedCycle.value)

        // When
        viewModel.selectCycle(cycle1)
        advanceUntilIdle()

        // Then
        assertEquals(cycle1, viewModel.selectedCycle.value)
        job.cancel()
    }

    @Test
    fun `expensesByCategory groups expenses correctly by category`() = runTest(testDispatcher) {
        // Given
        val cycle = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle))
        fakeBudgetCycleRepository.setCurrentCycle(cycle)

        val expenses = listOf(
            Expense(1, 1, housingCategoryId, "Loyer", 800.0, LocalDate.of(2025, 1, 5), true),
            Expense(2, 1, foodCategoryId, "Courses", 150.0, LocalDate.of(2025, 1, 10), false),
            Expense(3, 1, foodCategoryId, "Restaurant", 50.0, LocalDate.of(2025, 1, 15), false),
            Expense(4, 1, transportCategoryId, "Métro", 75.0, LocalDate.of(2025, 1, 8), false)
        )
        fakeExpenseRepository.setExpenses(1, expenses)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.expensesByCategory.collect {} }
        advanceUntilIdle()

        // Then
        val result = viewModel.expensesByCategory.value
        assertEquals(800.0, result[housingCategory] ?: 0.0, 0.01)
        assertEquals(200.0, result[foodCategory] ?: 0.0, 0.01) // 150 + 50
        assertEquals(75.0, result[transportCategory] ?: 0.0, 0.01)
        job.cancel()
    }

    @Test
    fun `expensesByCategory only contains expenses from selected cycle`() = runTest(testDispatcher) {
        // Given
        val cycle1 = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        val cycle2 = BudgetCycle(2, 2500.0, LocalDate.of(2025, 2, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle1, cycle2))
        fakeBudgetCycleRepository.setCurrentCycle(cycle2)

        fakeExpenseRepository.setExpenses(1, listOf(
            Expense(1, 1, housingCategoryId, "Loyer Janvier", 800.0, LocalDate.of(2025, 1, 5), true)
        ))
        fakeExpenseRepository.setExpenses(2, listOf(
            Expense(2, 2, housingCategoryId, "Loyer Février", 850.0, LocalDate.of(2025, 2, 5), true)
        ))

        viewModel = createViewModel()
        val job = launch { viewModel.expensesByCategory.collect {} }
        advanceUntilIdle()

        // Initially shows cycle2 expenses
        assertEquals(850.0, viewModel.expensesByCategory.value[housingCategory] ?: 0.0, 0.01)

        // When selecting cycle1
        viewModel.selectCycle(cycle1)
        advanceUntilIdle()

        // Then shows cycle1 expenses
        assertEquals(800.0, viewModel.expensesByCategory.value[housingCategory] ?: 0.0, 0.01)
        job.cancel()
    }

    @Test
    fun `totalExpenses calculates correct sum`() = runTest(testDispatcher) {
        // Given
        val cycle = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle))
        fakeBudgetCycleRepository.setCurrentCycle(cycle)

        val expenses = listOf(
            Expense(1, 1, housingCategoryId, "Loyer", 800.0, LocalDate.of(2025, 1, 5), true),
            Expense(2, 1, foodCategoryId, "Courses", 150.0, LocalDate.of(2025, 1, 10), false),
            Expense(3, 1, transportCategoryId, "Métro", 75.0, LocalDate.of(2025, 1, 8), false)
        )
        fakeExpenseRepository.setExpenses(1, expenses)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.totalExpenses.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(1025.0, viewModel.totalExpenses.value, 0.01) // 800 + 150 + 75
        job.cancel()
    }

    @Test
    fun `totalIncomes calculates correct sum`() = runTest(testDispatcher) {
        // Given
        val cycle = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle))
        fakeBudgetCycleRepository.setCurrentCycle(cycle)

        val incomes = listOf(
            Income(1, 1, "Prime", 500.0, LocalDate.of(2025, 1, 15), false),
            Income(2, 1, "Vente", 100.0, LocalDate.of(2025, 1, 20), false)
        )
        fakeIncomeRepository.setIncomes(1, incomes)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.totalIncomes.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(600.0, viewModel.totalIncomes.value, 0.01) // 500 + 100
        job.cancel()
    }

    @Test
    fun `balance equals selectedCycle amount plus totalIncomes minus totalExpenses`() = runTest(testDispatcher) {
        // Given
        val cycle = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle))
        fakeBudgetCycleRepository.setCurrentCycle(cycle)

        val expenses = listOf(
            Expense(1, 1, housingCategoryId, "Loyer", 800.0, LocalDate.of(2025, 1, 5), true),
            Expense(2, 1, foodCategoryId, "Courses", 200.0, LocalDate.of(2025, 1, 10), false)
        )
        fakeExpenseRepository.setExpenses(1, expenses)

        val incomes = listOf(
            Income(1, 1, "Prime", 300.0, LocalDate.of(2025, 1, 15), false)
        )
        fakeIncomeRepository.setIncomes(1, incomes)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.balance.collect {} }
        advanceUntilIdle()

        // Then
        // balance = cycle.amount + incomes - expenses = 2000 + 300 - 1000 = 1300
        assertEquals(1300.0, viewModel.balance.value, 0.01)
        job.cancel()
    }

    @Test
    fun `when cycle changes all data updates`() = runTest(testDispatcher) {
        // Given
        val cycle1 = BudgetCycle(1, 2000.0, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
        val cycle2 = BudgetCycle(2, 2500.0, LocalDate.of(2025, 2, 1), null)
        fakeBudgetCycleRepository.setCycles(listOf(cycle1, cycle2))
        fakeBudgetCycleRepository.setCurrentCycle(cycle2)

        fakeExpenseRepository.setExpenses(1, listOf(
            Expense(1, 1, foodCategoryId, "Dépense Jan", 500.0, LocalDate.of(2025, 1, 10), false)
        ))
        fakeExpenseRepository.setExpenses(2, listOf(
            Expense(2, 2, transportCategoryId, "Dépense Fév", 300.0, LocalDate.of(2025, 2, 10), false)
        ))

        fakeIncomeRepository.setIncomes(1, listOf(
            Income(1, 1, "Revenu Jan", 200.0, LocalDate.of(2025, 1, 15), false)
        ))
        fakeIncomeRepository.setIncomes(2, listOf(
            Income(2, 2, "Revenu Fév", 400.0, LocalDate.of(2025, 2, 15), false)
        ))

        viewModel = createViewModel()
        val jobExpenses = launch { viewModel.expensesByCategory.collect {} }
        val jobTotalExp = launch { viewModel.totalExpenses.collect {} }
        val jobTotalInc = launch { viewModel.totalIncomes.collect {} }
        val jobBalance = launch { viewModel.balance.collect {} }
        advanceUntilIdle()

        // Verify initial state (cycle2)
        assertEquals(300.0, viewModel.totalExpenses.value, 0.01)
        assertEquals(400.0, viewModel.totalIncomes.value, 0.01)
        assertEquals(2600.0, viewModel.balance.value, 0.01) // 2500 + 400 - 300

        // When selecting cycle1
        viewModel.selectCycle(cycle1)
        advanceUntilIdle()

        // Then all data updates
        assertEquals(500.0, viewModel.totalExpenses.value, 0.01)
        assertEquals(200.0, viewModel.totalIncomes.value, 0.01)
        assertEquals(1700.0, viewModel.balance.value, 0.01) // 2000 + 200 - 500
        assertEquals(500.0, viewModel.expensesByCategory.value[foodCategory] ?: 0.0, 0.01)
        assertNull(viewModel.expensesByCategory.value[transportCategory])

        jobExpenses.cancel()
        jobTotalExp.cancel()
        jobTotalInc.cancel()
        jobBalance.cancel()
    }
}

// Fake repositories for testing

class FakeBudgetCycleRepositoryForStats : IBudgetCycleRepository {
    private val _cycles = MutableStateFlow<List<BudgetCycle>>(emptyList())
    private val _currentCycle = MutableStateFlow<BudgetCycle?>(null)

    override val currentCycle: Flow<BudgetCycle?> = _currentCycle
    override val allCycles: Flow<List<BudgetCycle>> = _cycles

    override suspend fun insert(cycle: BudgetCycle): Long {
        val newCycle = cycle.copy(id = (_cycles.value.maxOfOrNull { it.id } ?: 0) + 1)
        _cycles.value = _cycles.value + newCycle
        return newCycle.id
    }

    override suspend fun update(cycle: BudgetCycle) {
        _cycles.value = _cycles.value.map { if (it.id == cycle.id) cycle else it }
    }

    override suspend fun delete(cycle: BudgetCycle) {
        _cycles.value = _cycles.value.filter { it.id != cycle.id }
    }

    override fun getById(id: Long): Flow<BudgetCycle?> = _cycles.map { cycles ->
        cycles.find { it.id == id }
    }

    fun setCycles(cycles: List<BudgetCycle>) {
        _cycles.value = cycles
    }

    fun setCurrentCycle(cycle: BudgetCycle?) {
        _currentCycle.value = cycle
    }
}

class FakeExpenseRepositoryForStats : IExpenseRepository {
    private val _expensesByCycle = mutableMapOf<Long, MutableStateFlow<List<Expense>>>()

    override suspend fun insert(expense: Expense): Long = expense.id

    override suspend fun update(expense: Expense) {}

    override suspend fun delete(expense: Expense) {}

    override fun getById(id: Long): Flow<Expense?> = MutableStateFlow(null)

    override fun getByCycleId(cycleId: Long): Flow<List<Expense>> {
        return _expensesByCycle.getOrPut(cycleId) { MutableStateFlow(emptyList()) }
    }

    override fun getFixedExpenses(cycleId: Long): Flow<List<Expense>> {
        return getByCycleId(cycleId).map { expenses -> expenses.filter { it.isFixed } }
    }

    override fun getVariableExpenses(cycleId: Long): Flow<List<Expense>> {
        return getByCycleId(cycleId).map { expenses -> expenses.filter { !it.isFixed } }
    }

    fun setExpenses(cycleId: Long, expenses: List<Expense>) {
        _expensesByCycle.getOrPut(cycleId) { MutableStateFlow(emptyList()) }.value = expenses
    }
}

class FakeIncomeRepositoryForStats : IIncomeRepository {
    private val _incomesByCycle = mutableMapOf<Long, MutableStateFlow<List<Income>>>()

    override suspend fun insert(income: Income): Long = income.id

    override suspend fun update(income: Income) {}

    override suspend fun delete(income: Income) {}

    override fun getById(id: Long): Flow<Income?> = MutableStateFlow(null)

    override fun getByCycleId(cycleId: Long): Flow<List<Income>> {
        return _incomesByCycle.getOrPut(cycleId) { MutableStateFlow(emptyList()) }
    }

    override fun getFixedIncomes(cycleId: Long): Flow<List<Income>> {
        return getByCycleId(cycleId).map { incomes -> incomes.filter { it.isFixed } }
    }

    override fun getVariableIncomes(cycleId: Long): Flow<List<Income>> {
        return getByCycleId(cycleId).map { incomes -> incomes.filter { !it.isFixed } }
    }

    fun setIncomes(cycleId: Long, incomes: List<Income>) {
        _incomesByCycle.getOrPut(cycleId) { MutableStateFlow(emptyList()) }.value = incomes
    }
}

class FakeCategoryRepositoryForStats : ICategoryRepository {
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
