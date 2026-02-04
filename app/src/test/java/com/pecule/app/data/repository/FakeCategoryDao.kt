package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.CategoryDao
import com.pecule.app.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCategoryDao : CategoryDao {
    private val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(category: CategoryEntity): Long {
        val id = if (category.id == 0L) nextId++ else category.id
        val newCategory = category.copy(id = id)
        categories.update { it + newCategory }
        return id
    }

    override suspend fun insertAll(categories: List<CategoryEntity>) {
        categories.forEach { insert(it) }
    }

    override suspend fun update(category: CategoryEntity) {
        categories.update { list ->
            list.map { if (it.id == category.id) category else it }
        }
    }

    override suspend fun delete(category: CategoryEntity) {
        categories.update { list ->
            list.filter { it.id != category.id }
        }
    }

    override fun getAll(): Flow<List<CategoryEntity>> = categories

    override fun getDefaultCategories(): Flow<List<CategoryEntity>> = categories.map { list ->
        list.filter { it.isDefault }
    }

    override fun getById(id: Long): Flow<CategoryEntity?> = categories.map { list ->
        list.find { it.id == id }
    }

    override suspend fun getCount(): Int = categories.value.size
}
