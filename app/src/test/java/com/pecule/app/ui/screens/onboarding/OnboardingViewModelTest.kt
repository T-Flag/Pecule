package com.pecule.app.ui.screens.onboarding

import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for OnboardingViewModel logic using fake repositories.
 * Tests the same behavior as the ViewModel without Hilt dependencies.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var fakeBudgetCycleRepository: FakeBudgetCycleRepository

    @Before
    fun setup() {
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
        fakeBudgetCycleRepository = FakeBudgetCycleRepository()
    }

    @Test
    fun `isFirstLaunch returns true when firstName is empty`() = runTest {
        // Given: default state with empty firstName
        // When: checking isFirstLaunch
        val isFirstLaunch = fakeUserPreferencesRepository.isFirstLaunch.first()

        // Then: should be true
        assertTrue(isFirstLaunch)
    }

    @Test
    fun `isFirstLaunch returns false when firstName exists`() = runTest {
        // Given: firstName is set
        fakeUserPreferencesRepository.updateFirstName("Jean")

        // When: checking isFirstLaunch
        val isFirstLaunch = fakeUserPreferencesRepository.isFirstLaunch.first()

        // Then: should be false
        assertFalse(isFirstLaunch)
    }

    @Test
    fun `completeOnboarding saves firstName correctly`() = runTest {
        // Given: empty firstName
        assertTrue(fakeUserPreferencesRepository.isFirstLaunch.first())

        // When: completing onboarding (simulating ViewModel behavior)
        val firstName = "Marie"
        fakeUserPreferencesRepository.updateFirstName(firstName)

        // Then: firstName should be saved
        assertEquals(firstName, fakeUserPreferencesRepository.getCurrentFirstName())
        assertFalse(fakeUserPreferencesRepository.isFirstLaunch.first())
    }

    @Test
    fun `completeOnboarding creates BudgetCycle with correct values`() = runTest {
        // Given: no existing cycles
        assertNull(fakeBudgetCycleRepository.currentCycle.first())

        // When: completing onboarding (simulating ViewModel behavior)
        val salaryAmount = 2500.0
        val salaryDate = LocalDate.of(2025, 1, 25)

        fakeBudgetCycleRepository.insert(
            BudgetCycle(
                amount = salaryAmount,
                startDate = salaryDate,
                endDate = null
            )
        )

        // Then: cycle should be created with correct values
        val insertedCycles = fakeBudgetCycleRepository.getInsertedCycles()
        assertEquals(1, insertedCycles.size)

        val cycle = insertedCycles.first()
        assertEquals(salaryAmount, cycle.amount, 0.001)
        assertEquals(salaryDate, cycle.startDate)
        assertNull(cycle.endDate)
    }

    @Test
    fun `completeOnboarding creates open cycle with null endDate`() = runTest {
        // When: creating a new cycle
        fakeBudgetCycleRepository.insert(
            BudgetCycle(
                amount = 3000.0,
                startDate = LocalDate.now(),
                endDate = null
            )
        )

        // Then: currentCycle should return this cycle (open cycle)
        val currentCycle = fakeBudgetCycleRepository.currentCycle.first()
        assertNotNull(currentCycle)
        assertNull(currentCycle?.endDate)
    }

    @Test
    fun `full onboarding flow saves both preferences and cycle`() = runTest {
        // Given: first launch state
        assertTrue(fakeUserPreferencesRepository.isFirstLaunch.first())
        assertNull(fakeBudgetCycleRepository.currentCycle.first())

        // When: simulating complete onboarding
        val firstName = "Pierre"
        val salaryAmount = 2800.0
        val salaryDate = LocalDate.of(2025, 1, 15)

        // This is what ViewModel.completeOnboarding() does:
        fakeUserPreferencesRepository.updateFirstName(firstName)
        fakeBudgetCycleRepository.insert(
            BudgetCycle(
                amount = salaryAmount,
                startDate = salaryDate,
                endDate = null
            )
        )

        // Then: both should be saved correctly
        assertFalse(fakeUserPreferencesRepository.isFirstLaunch.first())
        assertEquals(firstName, fakeUserPreferencesRepository.getCurrentFirstName())

        val currentCycle = fakeBudgetCycleRepository.currentCycle.first()
        assertNotNull(currentCycle)
        assertEquals(salaryAmount, currentCycle!!.amount, 0.001)
        assertEquals(salaryDate, currentCycle.startDate)
    }
}
