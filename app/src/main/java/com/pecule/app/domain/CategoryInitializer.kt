package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.repository.ICategoryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryInitializer @Inject constructor(
    private val categoryRepository: ICategoryRepository
) {
    companion object {
        // Couleurs correspondant au DonutChart
        private const val COLOR_SALARY = 0xFF4CAF50L       // Vert
        private const val COLOR_FOOD = 0xFFFF9800L         // Orange
        private const val COLOR_TRANSPORT = 0xFF2196F3L    // Bleu
        private const val COLOR_HOUSING = 0xFF9C27B0L      // Violet
        private const val COLOR_UTILITIES = 0xFFFFEB3BL    // Jaune
        private const val COLOR_ENTERTAINMENT = 0xFFE91E63L // Rose
        private const val COLOR_HEALTH = 0xFF00BCD4L       // Cyan
        private const val COLOR_SHOPPING = 0xFFFF5722L     // Orange foncé
        private const val COLOR_OTHER = 0xFF607D8BL        // Gris bleuté

        val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(
                id = 1,
                name = "Salaire",
                icon = "payments",
                color = COLOR_SALARY,
                isDefault = true
            ),
            CategoryEntity(
                id = 2,
                name = "Alimentation",
                icon = "restaurant",
                color = COLOR_FOOD,
                isDefault = true
            ),
            CategoryEntity(
                id = 3,
                name = "Transport",
                icon = "directions_car",
                color = COLOR_TRANSPORT,
                isDefault = true
            ),
            CategoryEntity(
                id = 4,
                name = "Logement",
                icon = "home",
                color = COLOR_HOUSING,
                isDefault = true
            ),
            CategoryEntity(
                id = 5,
                name = "Factures",
                icon = "receipt_long",
                color = COLOR_UTILITIES,
                isDefault = true
            ),
            CategoryEntity(
                id = 6,
                name = "Loisirs",
                icon = "sports_esports",
                color = COLOR_ENTERTAINMENT,
                isDefault = true
            ),
            CategoryEntity(
                id = 7,
                name = "Santé",
                icon = "medical_services",
                color = COLOR_HEALTH,
                isDefault = true
            ),
            CategoryEntity(
                id = 8,
                name = "Shopping",
                icon = "shopping_bag",
                color = COLOR_SHOPPING,
                isDefault = true
            ),
            CategoryEntity(
                id = 9,
                name = "Autre",
                icon = "more_horiz",
                color = COLOR_OTHER,
                isDefault = true
            )
        )
    }

    suspend fun initializeDefaultCategories() {
        val count = categoryRepository.getCount()
        if (count == 0) {
            categoryRepository.insertAll(DEFAULT_CATEGORIES)
        }
    }
}
