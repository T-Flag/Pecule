package com.pecule.app.ui.screens.onboarding

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class OnboardingValidationTest {

    // ==================== FirstName Validation ====================

    @Test
    fun `validateFirstName with empty string returns false`() {
        assertFalse(validateFirstName(""))
    }

    @Test
    fun `validateFirstName with spaces only returns false`() {
        assertFalse(validateFirstName("   "))
        assertFalse(validateFirstName("\t"))
        assertFalse(validateFirstName("\n"))
    }

    @Test
    fun `validateFirstName with valid name returns true`() {
        assertTrue(validateFirstName("Jean"))
        assertTrue(validateFirstName("Marie-Claire"))
        assertTrue(validateFirstName("François"))
    }

    @Test
    fun `validateFirstName with name and leading spaces returns true`() {
        assertTrue(validateFirstName("  Jean"))
        assertTrue(validateFirstName("Jean  "))
    }

    // ==================== Amount Validation ====================

    @Test
    fun `validateAmount with null returns false`() {
        assertFalse(validateAmount(null))
    }

    @Test
    fun `validateAmount with zero returns false`() {
        assertFalse(validateAmount(0.0))
    }

    @Test
    fun `validateAmount with negative value returns false`() {
        assertFalse(validateAmount(-100.0))
        assertFalse(validateAmount(-0.01))
    }

    @Test
    fun `validateAmount with positive value returns true`() {
        assertTrue(validateAmount(0.01))
        assertTrue(validateAmount(100.0))
        assertTrue(validateAmount(2500.50))
    }

    // ==================== Date Validation ====================

    @Test
    fun `validateDate with null returns false`() {
        assertFalse(validateDate(null))
    }

    @Test
    fun `validateDate with future date returns false`() {
        val today = LocalDate.of(2025, 1, 25)
        val futureDate = LocalDate.of(2025, 1, 26)

        assertFalse(validateDate(futureDate, today))
    }

    @Test
    fun `validateDate with today returns true`() {
        val today = LocalDate.of(2025, 1, 25)

        assertTrue(validateDate(today, today))
    }

    @Test
    fun `validateDate with past date returns true`() {
        val today = LocalDate.of(2025, 1, 25)
        val pastDate = LocalDate.of(2025, 1, 20)

        assertTrue(validateDate(pastDate, today))
    }

    // ==================== Full Validation ====================

    @Test
    fun `validateOnboardingInput all valid returns isValid true`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "Jean",
            amount = 2500.0,
            date = LocalDate.of(2025, 1, 20),
            today = today
        )

        assertTrue(result.isValid)
        assertTrue(result.isFirstNameValid)
        assertTrue(result.isAmountValid)
        assertTrue(result.isDateValid)
    }

    @Test
    fun `validateOnboardingInput with empty firstName returns isValid false`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "",
            amount = 2500.0,
            date = LocalDate.of(2025, 1, 20),
            today = today
        )

        assertFalse(result.isValid)
        assertFalse(result.isFirstNameValid)
        assertTrue(result.isAmountValid)
        assertTrue(result.isDateValid)
    }

    @Test
    fun `validateOnboardingInput with invalid amount returns isValid false`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "Jean",
            amount = 0.0,
            date = LocalDate.of(2025, 1, 20),
            today = today
        )

        assertFalse(result.isValid)
        assertTrue(result.isFirstNameValid)
        assertFalse(result.isAmountValid)
        assertTrue(result.isDateValid)
    }

    @Test
    fun `validateOnboardingInput with future date returns isValid false`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "Jean",
            amount = 2500.0,
            date = LocalDate.of(2025, 2, 1),
            today = today
        )

        assertFalse(result.isValid)
        assertTrue(result.isFirstNameValid)
        assertTrue(result.isAmountValid)
        assertFalse(result.isDateValid)
    }

    @Test
    fun `validateOnboardingInput with null date returns isValid false`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "Jean",
            amount = 2500.0,
            date = null,
            today = today
        )

        assertFalse(result.isValid)
        assertTrue(result.isFirstNameValid)
        assertTrue(result.isAmountValid)
        assertFalse(result.isDateValid)
    }

    @Test
    fun `validateOnboardingInput with multiple invalid fields`() {
        val today = LocalDate.of(2025, 1, 25)
        val result = validateOnboardingInput(
            firstName = "   ",
            amount = null,
            date = null,
            today = today
        )

        assertFalse(result.isValid)
        assertFalse(result.isFirstNameValid)
        assertFalse(result.isAmountValid)
        assertFalse(result.isDateValid)
    }
}
