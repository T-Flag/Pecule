package com.pecule.app.ui.screens.profile

import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.repository.ICategoryRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCategoryRepository: FakeCategoryRepositoryForCategories
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCategoryRepository = FakeCategoryRepositoryForCategories()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CategoriesViewModel {
        return CategoriesViewModel(fakeCategoryRepository)
    }

    @Test
    fun `categories contains all categories from repository`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)

        // When
        viewModel = createViewModel()
        val job = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        // Then
        assertEquals(9, viewModel.categories.value.size)
        job.cancel()
    }

    @Test
    fun `createCategory adds new category to repository`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        // When
        viewModel.createCategory("Vacances", "flight", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals(10, viewModel.categories.value.size)
        val newCategory = viewModel.categories.value.find { it.name == "Vacances" }
        assertNotNull(newCategory)
        assertEquals("flight", newCategory?.icon)
        assertEquals(0xFF2196F3L, newCategory?.color)
        assertEquals(false, newCategory?.isDefault)
        job.cancel()
    }

    @Test
    fun `createCategory with blank name shows error`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // When
        viewModel.createCategory("   ", "flight", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals("Le nom ne peut pas être vide", viewModel.saveError.value)
        job.cancel()
    }

    @Test
    fun `createCategory with duplicate name shows error`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // When: try to create a category with same name as existing
        viewModel.createCategory("Alimentation", "restaurant", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals("Cette catégorie existe déjà", viewModel.saveError.value)
        job.cancel()
    }

    @Test
    fun `createCategory with duplicate name case insensitive shows error`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // When: try to create a category with same name different case
        viewModel.createCategory("ALIMENTATION", "restaurant", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals("Cette catégorie existe déjà", viewModel.saveError.value)
        job.cancel()
    }

    @Test
    fun `updateCategory updates existing category`() = runTest(testDispatcher) {
        // Given
        val customCategory = CategoryEntity(
            id = 20,
            name = "Vacances",
            icon = "flight",
            color = 0xFF2196F3L,
            isDefault = false
        )
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES + customCategory)
        viewModel = createViewModel()
        val job = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateCategory(customCategory, "Voyages", "directions_car", 0xFF4CAF50L)
        advanceUntilIdle()

        // Then
        val updatedCategory = viewModel.categories.value.find { it.id == 20L }
        assertNotNull(updatedCategory)
        assertEquals("Voyages", updatedCategory?.name)
        assertEquals("directions_car", updatedCategory?.icon)
        assertEquals(0xFF4CAF50L, updatedCategory?.color)
        job.cancel()
    }

    @Test
    fun `updateCategory with blank name shows error`() = runTest(testDispatcher) {
        // Given
        val customCategory = CategoryEntity(
            id = 20,
            name = "Vacances",
            icon = "flight",
            color = 0xFF2196F3L,
            isDefault = false
        )
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES + customCategory)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // When
        viewModel.updateCategory(customCategory, "  ", "flight", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals("Le nom ne peut pas être vide", viewModel.saveError.value)
        job.cancel()
    }

    @Test
    fun `updateCategory with duplicate name shows error`() = runTest(testDispatcher) {
        // Given
        val customCategory = CategoryEntity(
            id = 20,
            name = "Vacances",
            icon = "flight",
            color = 0xFF2196F3L,
            isDefault = false
        )
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES + customCategory)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // When: try to rename to an existing category name
        viewModel.updateCategory(customCategory, "Alimentation", "flight", 0xFF2196F3L)
        advanceUntilIdle()

        // Then
        assertEquals("Cette catégorie existe déjà", viewModel.saveError.value)
        job.cancel()
    }

    @Test
    fun `deleteCategory removes non-default category`() = runTest(testDispatcher) {
        // Given
        val customCategory = CategoryEntity(
            id = 20,
            name = "Vacances",
            icon = "flight",
            color = 0xFF2196F3L,
            isDefault = false
        )
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES + customCategory)
        viewModel = createViewModel()
        val job = launch { viewModel.categories.collect {} }
        advanceUntilIdle()
        assertEquals(10, viewModel.categories.value.size)

        // When
        viewModel.deleteCategory(customCategory)
        advanceUntilIdle()

        // Then
        assertEquals(9, viewModel.categories.value.size)
        assertNull(viewModel.categories.value.find { it.id == 20L })
        job.cancel()
    }

    @Test
    fun `deleteCategory does not remove default category`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.categories.collect {} }
        advanceUntilIdle()

        val defaultCategory = viewModel.categories.value.first { it.isDefault }

        // When
        viewModel.deleteCategory(defaultCategory)
        advanceUntilIdle()

        // Then: category still exists
        assertEquals(9, viewModel.categories.value.size)
        assertNotNull(viewModel.categories.value.find { it.id == defaultCategory.id })
        job.cancel()
    }

    @Test
    fun `clearError clears saveError`() = runTest(testDispatcher) {
        // Given
        fakeCategoryRepository.setCategories(CategoryInitializer.DEFAULT_CATEGORIES)
        viewModel = createViewModel()
        val job = launch { viewModel.saveError.collect {} }
        advanceUntilIdle()

        // Create an error
        viewModel.createCategory("", "flight", 0xFF2196F3L)
        advanceUntilIdle()
        assertNotNull(viewModel.saveError.value)

        // When
        viewModel.clearError()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.saveError.value)
        job.cancel()
    }
}

// Fake Category Repository for Categories tests
class FakeCategoryRepositoryForCategories : ICategoryRepository {
    private val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categories

    override fun getDefaultCategories(): Flow<List<CategoryEntity>> = categories.map { list ->
        list.filter { it.isDefault }
    }

    override fun getById(id: Long): Flow<CategoryEntity?> = categories.map { list ->
        list.find { it.id == id }
    }

    override suspend fun insert(category: CategoryEntity): Long {
        val newId = (categories.value.maxOfOrNull { it.id } ?: 0) + 1
        val newCategory = category.copy(id = newId)
        categories.value = categories.value + newCategory
        return newId
    }

    override suspend fun insertAll(newCategories: List<CategoryEntity>) {
        categories.value = categories.value + newCategories
    }

    override suspend fun update(category: CategoryEntity) {
        categories.value = categories.value.map {
            if (it.id == category.id) category else it
        }
    }

    override suspend fun delete(category: CategoryEntity) {
        categories.value = categories.value.filter { it.id != category.id }
    }

    override suspend fun getCount(): Int = categories.value.size

    fun setCategories(newCategories: List<CategoryEntity>) {
        categories.value = newCategories
    }
}
