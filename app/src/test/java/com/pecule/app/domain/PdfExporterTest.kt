package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PdfExporterTest {

    private lateinit var pdfExporter: PdfExporter

    private val testCycle = BudgetCycle(
        id = 1,
        amount = 2500.0,
        startDate = LocalDate.of(2025, 1, 25),
        endDate = LocalDate.of(2025, 2, 24)
    )

    private val foodCategory = CategoryEntity(
        id = 2,
        name = "Alimentation",
        icon = "Restaurant",
        color = 0xFF4CAF50L,
        isDefault = true
    )

    private val transportCategory = CategoryEntity(
        id = 3,
        name = "Transport",
        icon = "DirectionsCar",
        color = 0xFF2196F3L,
        isDefault = true
    )

    @Before
    fun setup() {
        pdfExporter = PdfExporter()
    }

    @Test
    fun `generateTitle contains Pecule and Rapport`() {
        // When
        val title = pdfExporter.generateTitle(testCycle)

        // Then
        assertTrue(title.contains("Pécule"))
        assertTrue(title.contains("Rapport"))
    }

    @Test
    fun `generateTitle contains month and year from cycle`() {
        // When
        val title = pdfExporter.generateTitle(testCycle)

        // Then
        assertTrue(title.contains("janvier") || title.contains("Janvier"))
        assertTrue(title.contains("2025"))
    }

    @Test
    fun `calculateSummary returns correct totals`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 100.0, LocalDate.of(2025, 1, 28), false),
            Expense(2, 1, 3, "Essence", 50.0, LocalDate.of(2025, 1, 29), false)
        )
        val incomes = listOf(
            Income(1, 1, "Prime", 200.0, LocalDate.of(2025, 1, 26), false)
        )

        // When
        val summary = pdfExporter.calculateSummary(testCycle, expenses, incomes)

        // Then
        assertEquals(150.0, summary.totalExpenses, 0.01)
        assertEquals(200.0, summary.totalIncomes, 0.01)
        assertEquals(2550.0, summary.balance, 0.01) // 2500 + 200 - 150
    }

    @Test
    fun `calculateSummary includes budget amount`() {
        // Given
        val expenses = emptyList<Expense>()
        val incomes = emptyList<Income>()

        // When
        val summary = pdfExporter.calculateSummary(testCycle, expenses, incomes)

        // Then
        assertEquals(2500.0, summary.budgetAmount, 0.01)
        assertEquals(2500.0, summary.balance, 0.01)
    }

    @Test
    fun `calculateCategoryBreakdown groups expenses by category`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 100.0, LocalDate.of(2025, 1, 28), false),
            Expense(2, 1, 2, "Restaurant", 50.0, LocalDate.of(2025, 1, 29), false),
            Expense(3, 1, 3, "Essence", 75.0, LocalDate.of(2025, 1, 30), false)
        )
        val categories = mapOf(
            2L to foodCategory,
            3L to transportCategory
        )

        // When
        val categoryBreakdown = pdfExporter.calculateCategoryBreakdown(expenses, categories)

        // Then
        assertEquals(2, categoryBreakdown.size)
        assertTrue(categoryBreakdown.any { it.categoryName == "Alimentation" && it.amount == 150.0 })
        assertTrue(categoryBreakdown.any { it.categoryName == "Transport" && it.amount == 75.0 })
    }

    @Test
    fun `calculateCategoryBreakdown calculates correct percentages`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 100.0, LocalDate.of(2025, 1, 28), false),
            Expense(2, 1, 3, "Essence", 100.0, LocalDate.of(2025, 1, 30), false)
        )
        val categories = mapOf(
            2L to foodCategory,
            3L to transportCategory
        )

        // When
        val categoryBreakdown = pdfExporter.calculateCategoryBreakdown(expenses, categories)

        // Then
        categoryBreakdown.forEach { item ->
            assertEquals(50.0, item.percentage, 0.01)
        }
    }

    @Test
    fun `calculateCategoryBreakdown returns empty list for no expenses`() {
        // Given
        val expenses = emptyList<Expense>()
        val categories = emptyMap<Long, CategoryEntity>()

        // When
        val categoryBreakdown = pdfExporter.calculateCategoryBreakdown(expenses, categories)

        // Then
        assertTrue(categoryBreakdown.isEmpty())
    }

    @Test
    fun `calculateCategoryBreakdown is sorted by amount descending`() {
        // Given
        val expenses = listOf(
            Expense(1, 1, 2, "Courses", 50.0, LocalDate.of(2025, 1, 28), false),
            Expense(2, 1, 3, "Essence", 200.0, LocalDate.of(2025, 1, 30), false)
        )
        val categories = mapOf(
            2L to foodCategory,
            3L to transportCategory
        )

        // When
        val categoryBreakdown = pdfExporter.calculateCategoryBreakdown(expenses, categories)

        // Then
        assertEquals("Transport", categoryBreakdown[0].categoryName)
        assertEquals("Alimentation", categoryBreakdown[1].categoryName)
    }
}
