package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryTest {

    private lateinit var fakeDao: FakeCategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        fakeDao = FakeCategoryDao()
        repository = CategoryRepository(fakeDao)
    }

    // ==================== GET ALL CATEGORIES ====================

    @Test
    fun `getAllCategories returns all categories`() = runTest {
        // Given: des catégories par défaut et custom
        fakeDao.insert(CategoryEntity(id = 1, name = "Alimentation", icon = "restaurant", color = 0xFFFF9800, isDefault = true))
        fakeDao.insert(CategoryEntity(id = 2, name = "Transport", icon = "directions_car", color = 0xFF2196F3, isDefault = true))
        fakeDao.insert(CategoryEntity(id = 3, name = "Ma catégorie", icon = "star", color = 0xFFFFFFFF, isDefault = false))

        // When
        val categories = repository.getAllCategories().first()

        // Then: retourne toutes les catégories
        assertEquals(3, categories.size)
    }

    // ==================== GET DEFAULT CATEGORIES ====================

    @Test
    fun `getDefaultCategories returns only default categories`() = runTest {
        // Given: des catégories par défaut et custom
        fakeDao.insert(CategoryEntity(id = 1, name = "Alimentation", icon = "restaurant", color = 0xFFFF9800, isDefault = true))
        fakeDao.insert(CategoryEntity(id = 2, name = "Transport", icon = "directions_car", color = 0xFF2196F3, isDefault = true))
        fakeDao.insert(CategoryEntity(id = 3, name = "Ma catégorie", icon = "star", color = 0xFFFFFFFF, isDefault = false))

        // When
        val defaultCategories = repository.getDefaultCategories().first()

        // Then: retourne uniquement les catégories par défaut
        assertEquals(2, defaultCategories.size)
        assertTrue(defaultCategories.all { it.isDefault })
    }

    // ==================== INSERT ====================

    @Test
    fun `insert creates new custom category`() = runTest {
        // Given: une nouvelle catégorie custom
        val customCategory = CategoryEntity(
            name = "Épargne",
            icon = "savings",
            color = 0xFF00FF00,
            isDefault = false
        )

        // When
        val id = repository.insert(customCategory)

        // Then: catégorie créée avec un ID
        assertTrue(id > 0)
        val retrieved = repository.getById(id).first()
        assertNotNull(retrieved)
        assertEquals("Épargne", retrieved?.name)
        assertEquals(false, retrieved?.isDefault)
    }

    // ==================== UPDATE ====================

    @Test
    fun `update modifies existing category`() = runTest {
        // Given: une catégorie existante
        val id = fakeDao.insert(CategoryEntity(
            name = "Ancienne",
            icon = "old_icon",
            color = 0xFF000000,
            isDefault = false
        ))
        val existing = fakeDao.getById(id).first()!!

        // When: mise à jour
        val updated = existing.copy(name = "Nouvelle", icon = "new_icon", color = 0xFFFFFFFF)
        repository.update(updated)

        // Then: modifications appliquées
        val result = repository.getById(id).first()
        assertEquals("Nouvelle", result?.name)
        assertEquals("new_icon", result?.icon)
        assertEquals(0xFFFFFFFF, result?.color)
    }

    // ==================== DELETE ====================

    @Test
    fun `delete removes custom category`() = runTest {
        // Given: une catégorie custom
        val id = fakeDao.insert(CategoryEntity(
            name = "À supprimer",
            icon = "delete",
            color = 0xFFFF0000,
            isDefault = false
        ))
        val category = fakeDao.getById(id).first()!!

        // When
        repository.delete(category)

        // Then: catégorie supprimée
        val result = repository.getById(id).first()
        assertNull(result)
    }

    @Test
    fun `delete on default category does nothing`() = runTest {
        // Given: une catégorie par défaut
        val id = fakeDao.insert(CategoryEntity(
            name = "Alimentation",
            icon = "restaurant",
            color = 0xFFFF9800,
            isDefault = true
        ))
        val defaultCategory = fakeDao.getById(id).first()!!

        // When: tentative de suppression
        repository.delete(defaultCategory)

        // Then: catégorie toujours présente
        val result = repository.getById(id).first()
        assertNotNull(result)
        assertEquals("Alimentation", result?.name)
    }

    // ==================== FLOW UPDATE ====================

    @Test
    fun `after insert list updates via Flow`() = runTest {
        // Given: liste initiale vide
        val initialList = repository.getAllCategories().first()
        assertEquals(0, initialList.size)

        // When: insertion
        fakeDao.insert(CategoryEntity(
            name = "Nouvelle",
            icon = "new",
            color = 0xFF123456,
            isDefault = false
        ))

        // Then: liste mise à jour
        val updatedList = repository.getAllCategories().first()
        assertEquals(1, updatedList.size)
        assertEquals("Nouvelle", updatedList.first().name)
    }
}
