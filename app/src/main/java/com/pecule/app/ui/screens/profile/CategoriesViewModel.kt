package com.pecule.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.repository.ICategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: ICategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    fun clearError() {
        _saveError.value = null
    }

    fun createCategory(name: String, icon: String, color: Long) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isBlank()) {
                _saveError.value = "Le nom ne peut pas être vide"
                return@launch
            }

            val existingCategories = categoryRepository.getAllCategories().first()
            if (existingCategories.any { it.name.equals(trimmedName, ignoreCase = true) }) {
                _saveError.value = "Cette catégorie existe déjà"
                return@launch
            }

            categoryRepository.insert(
                CategoryEntity(
                    name = trimmedName,
                    icon = icon,
                    color = color,
                    isDefault = false
                )
            )
            _saveError.value = null
        }
    }

    fun updateCategory(category: CategoryEntity, newName: String, newIcon: String, newColor: Long) {
        viewModelScope.launch {
            val trimmedName = newName.trim()
            if (trimmedName.isBlank()) {
                _saveError.value = "Le nom ne peut pas être vide"
                return@launch
            }

            val existingCategories = categoryRepository.getAllCategories().first()
            if (existingCategories.any { it.id != category.id && it.name.equals(trimmedName, ignoreCase = true) }) {
                _saveError.value = "Cette catégorie existe déjà"
                return@launch
            }

            categoryRepository.update(
                category.copy(
                    name = trimmedName,
                    icon = newIcon,
                    color = newColor
                )
            )
            _saveError.value = null
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (category.isDefault) return
        viewModelScope.launch {
            categoryRepository.delete(category)
        }
    }
}
