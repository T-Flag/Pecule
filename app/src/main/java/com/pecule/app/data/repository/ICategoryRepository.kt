package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {
    suspend fun insert(category: CategoryEntity): Long
    suspend fun insertAll(categories: List<CategoryEntity>)
    suspend fun update(category: CategoryEntity)
    suspend fun delete(category: CategoryEntity)
    fun getAllCategories(): Flow<List<CategoryEntity>>
    fun getDefaultCategories(): Flow<List<CategoryEntity>>
    fun getById(id: Long): Flow<CategoryEntity?>
    suspend fun getCount(): Int
}
