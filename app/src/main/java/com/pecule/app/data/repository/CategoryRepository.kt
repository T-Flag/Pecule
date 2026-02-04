package com.pecule.app.data.repository

import com.pecule.app.data.local.database.dao.CategoryDao
import com.pecule.app.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) : ICategoryRepository {

    override suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)

    override suspend fun insertAll(categories: List<CategoryEntity>) = categoryDao.insertAll(categories)

    override suspend fun update(category: CategoryEntity) = categoryDao.update(category)

    override suspend fun delete(category: CategoryEntity) {
        // Ne pas supprimer les catégories par défaut
        if (!category.isDefault) {
            categoryDao.delete(category)
        }
    }

    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    override fun getDefaultCategories(): Flow<List<CategoryEntity>> = categoryDao.getDefaultCategories()

    override fun getById(id: Long): Flow<CategoryEntity?> = categoryDao.getById(id)

    override suspend fun getCount(): Int = categoryDao.getCount()
}
