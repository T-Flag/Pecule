package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TransactionDialogValidationTest {

    // ==================== LABEL TESTS ====================

    @Test
    fun `label vide retourne erreur`() {
        val errors = validateTransaction(
            label = "",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("Le libellé est requis"))
    }

    @Test
    fun `label avec espaces uniquement retourne erreur`() {
        val errors = validateTransaction(
            label = "   ",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("Le libellé est requis"))
    }

    @Test
    fun `label valide ne retourne pas erreur`() {
        val errors = validateTransaction(
            label = "Courses",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.none { it.contains("libellé") })
    }

    // ==================== AMOUNT TESTS ====================

    @Test
    fun `montant null retourne erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = null,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("Le montant est requis"))
    }

    @Test
    fun `montant egal zero retourne erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 0.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("Le montant doit être supérieur à 0"))
    }

    @Test
    fun `montant negatif retourne erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = -50.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("Le montant doit être supérieur à 0"))
    }

    @Test
    fun `montant positif ne retourne pas erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.none { it.contains("montant") })
    }

    // ==================== CATEGORY TESTS ====================

    @Test
    fun `depense sans categorie retourne erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 100.0,
            category = null,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.contains("La catégorie est requise"))
    }

    @Test
    fun `depense avec categorie ne retourne pas erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.none { it.contains("catégorie") })
    }

    @Test
    fun `revenu sans categorie ne retourne pas erreur`() {
        val errors = validateTransaction(
            label = "Prime",
            amount = 500.0,
            category = null,
            date = LocalDate.now(),
            isExpense = false
        )

        assertTrue(errors.none { it.contains("catégorie") })
    }

    // ==================== DATE TESTS ====================

    @Test
    fun `date null retourne erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 100.0,
            category = Category.FOOD,
            date = null,
            isExpense = true
        )

        assertTrue(errors.contains("La date est requise"))
    }

    @Test
    fun `date valide ne retourne pas erreur`() {
        val errors = validateTransaction(
            label = "Test",
            amount = 100.0,
            category = Category.FOOD,
            date = LocalDate.now(),
            isExpense = true
        )

        assertTrue(errors.none { it.contains("date") })
    }

    // ==================== COMPLETE VALIDATION TESTS ====================

    @Test
    fun `depense valide retourne liste vide`() {
        val errors = validateTransaction(
            label = "Courses Carrefour",
            amount = 85.50,
            category = Category.FOOD,
            date = LocalDate.of(2025, 1, 15),
            isExpense = true
        )

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `revenu valide retourne liste vide`() {
        val errors = validateTransaction(
            label = "Prime annuelle",
            amount = 1500.0,
            category = null,
            date = LocalDate.of(2025, 1, 20),
            isExpense = false
        )

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `plusieurs erreurs retourne toutes les erreurs`() {
        val errors = validateTransaction(
            label = "",
            amount = null,
            category = null,
            date = null,
            isExpense = true
        )

        assertEquals(4, errors.size)
        assertTrue(errors.contains("Le libellé est requis"))
        assertTrue(errors.contains("Le montant est requis"))
        assertTrue(errors.contains("La catégorie est requise"))
        assertTrue(errors.contains("La date est requise"))
    }

    @Test
    fun `revenu avec plusieurs erreurs ne compte pas categorie`() {
        val errors = validateTransaction(
            label = "",
            amount = -10.0,
            category = null,
            date = null,
            isExpense = false
        )

        assertEquals(3, errors.size)
        assertTrue(errors.contains("Le libellé est requis"))
        assertTrue(errors.contains("Le montant doit être supérieur à 0"))
        assertTrue(errors.contains("La date est requise"))
        assertTrue(errors.none { it.contains("catégorie") })
    }
}
