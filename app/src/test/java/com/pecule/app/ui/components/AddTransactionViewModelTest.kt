package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.domain.CategoryInitializer
import com.pecule.app.domain.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeExpenseRepository: FakeExpenseRepositoryForDialog
    private lateinit var fakeIncomeRepository: FakeIncomeRepositoryForDialog
    private lateinit var viewModel: AddTransactionViewModel

    // Category helpers
    private val foodCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Alimentation" }!!
    private val transportCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Transport" }!!
    private val otherCategory = CategoryInitializer.DEFAULT_CATEGORIES.find { it.name == "Autre" }!!

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExpenseRepository = FakeExpenseRepositoryForDialog()
        fakeIncomeRepository = FakeIncomeRepositoryForDialog()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== INITIAL STATE (CREATION MODE - EXPENSE) ====================

    @Test
    fun `etat initial mode creation depense - label est vide`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertEquals("", viewModel.uiState.first().label)
    }

    @Test
    fun `etat initial mode creation depense - amount est null`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertNull(viewModel.uiState.first().amount)
    }

    @Test
    fun `etat initial mode creation depense - category est null`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertNull(viewModel.uiState.first().category)
    }

    @Test
    fun `etat initial mode creation depense - date est aujourdhui`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertEquals(LocalDate.now(), viewModel.uiState.first().date)
    }

    @Test
    fun `etat initial mode creation depense - isFixed est false`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertFalse(viewModel.uiState.first().isFixed)
    }

    @Test
    fun `etat initial mode creation depense - isExpense est true`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertTrue(viewModel.uiState.first().isExpense)
    }

    @Test
    fun `etat initial mode creation depense - isEditing est false`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertFalse(viewModel.uiState.first().isEditing)
    }

    // ==================== INITIAL STATE (CREATION MODE - INCOME) ====================

    @Test
    fun `etat initial mode creation revenu - isExpense est false`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = false,
            cycleId = 1L,
            existingTransaction = null
        )

        assertFalse(viewModel.uiState.first().isExpense)
    }

    @Test
    fun `mode revenu - category reste null meme si on essaie de la definir`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = false,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateCategory(foodCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.first().category)
    }

    // ==================== EDIT MODE ====================

    @Test
    fun `mode edition - champs pre-remplis avec transaction existante`() = runTest(testDispatcher) {
        val existingTransaction = Transaction(
            id = 5L,
            label = "Courses existantes",
            amount = 75.50,
            date = LocalDate.of(2025, 1, 10),
            isExpense = true,
            isFixed = true,
            category = foodCategory
        )

        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = existingTransaction
        )

        val state = viewModel.uiState.first()
        assertEquals("Courses existantes", state.label)
        assertEquals(75.50, state.amount)
        assertEquals(LocalDate.of(2025, 1, 10), state.date)
        assertTrue(state.isFixed)
        assertEquals(foodCategory, state.category)
    }

    @Test
    fun `mode edition - isEditing est true`() = runTest(testDispatcher) {
        val existingTransaction = Transaction(
            id = 5L,
            label = "Test",
            amount = 100.0,
            date = LocalDate.now(),
            isExpense = true,
            isFixed = false,
            category = otherCategory
        )

        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = existingTransaction
        )

        assertTrue(viewModel.uiState.first().isEditing)
    }

    // ==================== ACTIONS ====================

    @Test
    fun `updateLabel met a jour le label`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateLabel("Nouveau label")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Nouveau label", viewModel.uiState.first().label)
    }

    @Test
    fun `updateAmount met a jour le montant`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateAmount(150.75)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(150.75, viewModel.uiState.first().amount)
    }

    @Test
    fun `updateCategory met a jour la categorie pour depense`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateCategory(transportCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(transportCategory, viewModel.uiState.first().category)
    }

    @Test
    fun `updateDate met a jour la date`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        val newDate = LocalDate.of(2025, 2, 15)
        viewModel.updateDate(newDate)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(newDate, viewModel.uiState.first().date)
    }

    @Test
    fun `toggleIsFixed inverse isFixed de false a true`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        assertFalse(viewModel.uiState.first().isFixed)

        viewModel.toggleIsFixed()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.first().isFixed)
    }

    @Test
    fun `toggleIsFixed inverse isFixed de true a false`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.toggleIsFixed() // false -> true
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleIsFixed() // true -> false
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().isFixed)
    }

    // ==================== SAVE TESTS ====================

    @Test
    fun `save avec donnees invalides ne sauvegarde pas`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        // Don't fill any fields, try to save
        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(result.isSuccess)
        assertTrue(fakeExpenseRepository.getInsertedExpenses().isEmpty())
    }

    @Test
    fun `save avec donnees invalides retourne les erreurs`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors.contains("Le libellé est requis"))
    }

    @Test
    fun `save depense valide appelle ExpenseRepository insert`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateLabel("Courses")
        viewModel.updateAmount(85.50)
        viewModel.updateCategory(foodCategory)
        viewModel.updateDate(LocalDate.of(2025, 1, 20))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(1, fakeExpenseRepository.getInsertedExpenses().size)

        val insertedExpense = fakeExpenseRepository.getInsertedExpenses().first()
        assertEquals("Courses", insertedExpense.label)
        assertEquals(85.50, insertedExpense.amount, 0.001)
        assertEquals(foodCategory.id, insertedExpense.categoryId)
    }

    @Test
    fun `save revenu valide appelle IncomeRepository insert`() = runTest(testDispatcher) {
        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = false,
            cycleId = 1L,
            existingTransaction = null
        )

        viewModel.updateLabel("Prime")
        viewModel.updateAmount(500.0)
        viewModel.updateDate(LocalDate.of(2025, 1, 25))
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertEquals(1, fakeIncomeRepository.getInsertedIncomes().size)
        assertTrue(fakeExpenseRepository.getInsertedExpenses().isEmpty())

        val insertedIncome = fakeIncomeRepository.getInsertedIncomes().first()
        assertEquals("Prime", insertedIncome.label)
        assertEquals(500.0, insertedIncome.amount, 0.001)
    }

    @Test
    fun `save en mode edition appelle update au lieu de insert`() = runTest(testDispatcher) {
        val existingTransaction = Transaction(
            id = 5L,
            label = "Ancien label",
            amount = 50.0,
            date = LocalDate.of(2025, 1, 10),
            isExpense = true,
            isFixed = false,
            category = otherCategory
        )

        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = true,
            cycleId = 1L,
            existingTransaction = existingTransaction
        )

        viewModel.updateLabel("Nouveau label")
        viewModel.updateAmount(100.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(fakeExpenseRepository.getInsertedExpenses().isEmpty())
        assertEquals(1, fakeExpenseRepository.getUpdatedExpenses().size)

        val updatedExpense = fakeExpenseRepository.getUpdatedExpenses().first()
        assertEquals(5L, updatedExpense.id)
        assertEquals("Nouveau label", updatedExpense.label)
        assertEquals(100.0, updatedExpense.amount, 0.001)
    }

    @Test
    fun `save revenu en mode edition appelle IncomeRepository update`() = runTest(testDispatcher) {
        val existingTransaction = Transaction(
            id = 3L,
            label = "Ancienne prime",
            amount = 200.0,
            date = LocalDate.of(2025, 1, 5),
            isExpense = false,
            isFixed = true,
            category = null
        )

        viewModel = AddTransactionViewModel(
            expenseRepository = fakeExpenseRepository,
            incomeRepository = fakeIncomeRepository,
            isExpense = false,
            cycleId = 1L,
            existingTransaction = existingTransaction
        )

        viewModel.updateLabel("Nouvelle prime")
        viewModel.updateAmount(300.0)
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(result.isSuccess)
        assertTrue(fakeIncomeRepository.getInsertedIncomes().isEmpty())
        assertEquals(1, fakeIncomeRepository.getUpdatedIncomes().size)

        val updatedIncome = fakeIncomeRepository.getUpdatedIncomes().first()
        assertEquals(3L, updatedIncome.id)
        assertEquals("Nouvelle prime", updatedIncome.label)
    }
}

// ==================== FAKE REPOSITORIES ====================

class FakeExpenseRepositoryForDialog : IExpenseRepository {
    private val expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val insertedExpenses = mutableListOf<Expense>()
    private val updatedExpenses = mutableListOf<Expense>()
    private var nextId = 1L

    override suspend fun insert(expense: Expense): Long {
        val id = nextId++
        val newExpense = expense.copy(id = id)
        insertedExpenses.add(newExpense)
        expenses.value = expenses.value + newExpense
        return id
    }

    override suspend fun update(expense: Expense) {
        updatedExpenses.add(expense)
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

    fun getInsertedExpenses(): List<Expense> = insertedExpenses.toList()
    fun getUpdatedExpenses(): List<Expense> = updatedExpenses.toList()
}

class FakeIncomeRepositoryForDialog : IIncomeRepository {
    private val incomes = MutableStateFlow<List<Income>>(emptyList())
    private val insertedIncomes = mutableListOf<Income>()
    private val updatedIncomes = mutableListOf<Income>()
    private var nextId = 1L

    override suspend fun insert(income: Income): Long {
        val id = nextId++
        val newIncome = income.copy(id = id)
        insertedIncomes.add(newIncome)
        incomes.value = incomes.value + newIncome
        return id
    }

    override suspend fun update(income: Income) {
        updatedIncomes.add(income)
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

    fun getInsertedIncomes(): List<Income> = insertedIncomes.toList()
    fun getUpdatedIncomes(): List<Income> = updatedIncomes.toList()
}
