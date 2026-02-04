package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CsvExporterTest {

    private val csvExporter = CsvExporter()

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

    @Test
    fun `export cycle without transactions returns CSV with headers only`() {
        // Given: empty lists
        val expenses = emptyList<Expense>()
        val incomes = emptyList<Income>()
        val categories = emptyMap<Long, CategoryEntity>()

        // When
        val csv = csvExporter.export(testCycle, expenses, incomes, categories)

        // Then
        val lines = csv.lines()
        assertEquals(1, lines.size)
        assertEquals("Type,Libellé,Catégorie,Montant,Date,Fixe", lines[0])
    }

    @Test
    fun `export cycle with expenses and incomes returns correctly formatted CSV`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Courses Carrefour",
                amount = 85.50,
                date = LocalDate.of(2025, 1, 28),
                isFixed = false
            ),
            Expense(
                id = 2,
                cycleId = 1,
                categoryId = 3,
                label = "Essence",
                amount = 62.00,
                date = LocalDate.of(2025, 1, 30),
                isFixed = false
            )
        )
        val incomes = listOf(
            Income(
                id = 1,
                cycleId = 1,
                label = "Prime",
                amount = 200.00,
                date = LocalDate.of(2025, 1, 26),
                isFixed = false
            )
        )
        val categories = mapOf(
            2L to foodCategory,
            3L to transportCategory
        )

        // When
        val csv = csvExporter.export(testCycle, expenses, incomes, categories)

        // Then
        val lines = csv.lines()
        assertEquals(4, lines.size)
        assertEquals("Type,Libellé,Catégorie,Montant,Date,Fixe", lines[0])
        assertTrue(lines.any { it.contains("Dépense") && it.contains("Courses Carrefour") })
        assertTrue(lines.any { it.contains("Dépense") && it.contains("Essence") })
        assertTrue(lines.any { it.contains("Revenu") && it.contains("Prime") })
    }

    @Test
    fun `expenses have type Depense`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Test",
                amount = 50.0,
                date = LocalDate.of(2025, 1, 28),
                isFixed = false
            )
        )
        val categories = mapOf(2L to foodCategory)

        // When
        val csv = csvExporter.export(testCycle, expenses, emptyList(), categories)

        // Then
        val lines = csv.lines()
        assertTrue(lines[1].startsWith("Dépense,"))
    }

    @Test
    fun `incomes have type Revenu`() {
        // Given
        val incomes = listOf(
            Income(
                id = 1,
                cycleId = 1,
                label = "Salaire",
                amount = 2500.0,
                date = LocalDate.of(2025, 1, 25),
                isFixed = true
            )
        )

        // When
        val csv = csvExporter.export(testCycle, emptyList(), incomes, emptyMap())

        // Then
        val lines = csv.lines()
        assertTrue(lines[1].startsWith("Revenu,"))
    }

    @Test
    fun `amount uses decimal point`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Test",
                amount = 85.50,
                date = LocalDate.of(2025, 1, 28),
                isFixed = false
            )
        )
        val categories = mapOf(2L to foodCategory)

        // When
        val csv = csvExporter.export(testCycle, expenses, emptyList(), categories)

        // Then
        assertTrue(csv.contains("85.50") || csv.contains("85.5"))
    }

    @Test
    fun `date format is DD-MM-YYYY`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Test",
                amount = 50.0,
                date = LocalDate.of(2025, 1, 28),
                isFixed = false
            )
        )
        val categories = mapOf(2L to foodCategory)

        // When
        val csv = csvExporter.export(testCycle, expenses, emptyList(), categories)

        // Then
        assertTrue(csv.contains("28/01/2025"))
    }

    @Test
    fun `fixed transactions show Oui, variable show Non`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Loyer",
                amount = 800.0,
                date = LocalDate.of(2025, 1, 28),
                isFixed = true
            ),
            Expense(
                id = 2,
                cycleId = 1,
                categoryId = 2,
                label = "Courses",
                amount = 50.0,
                date = LocalDate.of(2025, 1, 29),
                isFixed = false
            )
        )
        val categories = mapOf(2L to foodCategory)

        // When
        val csv = csvExporter.export(testCycle, expenses, emptyList(), categories)

        // Then
        val lines = csv.lines()
        val loyerLine = lines.find { it.contains("Loyer") }
        val coursesLine = lines.find { it.contains("Courses") }
        assertTrue(loyerLine?.endsWith(",Oui") == true)
        assertTrue(coursesLine?.endsWith(",Non") == true)
    }

    @Test
    fun `category name is included for expenses`() {
        // Given
        val expenses = listOf(
            Expense(
                id = 1,
                cycleId = 1,
                categoryId = 2,
                label = "Test",
                amount = 50.0,
                date = LocalDate.of(2025, 1, 28),
                isFixed = false
            )
        )
        val categories = mapOf(2L to foodCategory)

        // When
        val csv = csvExporter.export(testCycle, expenses, emptyList(), categories)

        // Then
        assertTrue(csv.contains("Alimentation"))
    }

    @Test
    fun `income without category shows empty category`() {
        // Given
        val incomes = listOf(
            Income(
                id = 1,
                cycleId = 1,
                label = "Prime",
                amount = 200.0,
                date = LocalDate.of(2025, 1, 26),
                isFixed = false
            )
        )

        // When
        val csv = csvExporter.export(testCycle, emptyList(), incomes, emptyMap())

        // Then
        val lines = csv.lines()
        // Format: Type,Libellé,Catégorie,Montant,Date,Fixe
        // Revenu,Prime,,200.0,26/01/2025,Non
        val incomeLine = lines[1]
        val parts = incomeLine.split(",")
        assertEquals("", parts[2]) // Category should be empty
    }
}
