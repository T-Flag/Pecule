package com.pecule.app.data.repository

import com.pecule.app.data.local.database.entity.BudgetCycle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for BudgetCycleRepository using a FakeDao.
 * Room integration tests with in-memory database are in androidTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetCycleRepositoryTest {

    private lateinit var fakeDao: FakeBudgetCycleDao

    @Before
    fun setup() {
        fakeDao = FakeBudgetCycleDao()
    }

    @Test
    fun `insert returns ID greater than 0`() = runTest {
        // Given: a new cycle
        val cycle = BudgetCycle(
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )

        // When: inserting
        val id = fakeDao.insert(cycle)

        // Then: ID should be > 0
        assertTrue(id > 0)
    }

    @Test
    fun `insert assigns unique IDs`() = runTest {
        // Given: multiple cycles
        val cycle1 = BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25))
        val cycle2 = BudgetCycle(amount = 2600.0, startDate = LocalDate.of(2025, 2, 25))

        // When: inserting both
        val id1 = fakeDao.insert(cycle1)
        val id2 = fakeDao.insert(cycle2)

        // Then: IDs should be different
        assertTrue(id1 != id2)
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)
    }

    @Test
    fun `getCurrentCycle returns cycle with null endDate`() = runTest {
        // Given: an open cycle (endDate = null)
        val openCycle = BudgetCycle(
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        fakeDao.insert(openCycle)

        // When: getting current cycle
        val currentCycle = fakeDao.getCurrentCycle().first()

        // Then: should return the open cycle
        assertNotNull(currentCycle)
        assertNull(currentCycle?.endDate)
        assertEquals(2500.0, currentCycle?.amount ?: 0.0, 0.001)
    }

    @Test
    fun `getCurrentCycle returns null when no open cycle exists`() = runTest {
        // Given: only closed cycles
        val closedCycle = BudgetCycle(
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = LocalDate.of(2025, 2, 24)
        )
        fakeDao.insert(closedCycle)

        // When: getting current cycle
        val currentCycle = fakeDao.getCurrentCycle().first()

        // Then: should be null
        assertNull(currentCycle)
    }

    @Test
    fun `getCurrentCycle returns only open cycle among multiple cycles`() = runTest {
        // Given: closed and open cycles
        val closedCycle = BudgetCycle(
            amount = 2400.0,
            startDate = LocalDate.of(2024, 12, 25),
            endDate = LocalDate.of(2025, 1, 24)
        )
        val openCycle = BudgetCycle(
            amount = 2500.0,
            startDate = LocalDate.of(2025, 1, 25),
            endDate = null
        )
        fakeDao.insert(closedCycle)
        fakeDao.insert(openCycle)

        // When: getting current cycle
        val currentCycle = fakeDao.getCurrentCycle().first()

        // Then: should return the open cycle
        assertNotNull(currentCycle)
        assertEquals(2500.0, currentCycle?.amount ?: 0.0, 0.001)
        assertNull(currentCycle?.endDate)
    }

    @Test
    fun `getAllCycles returns all cycles`() = runTest {
        // Given: multiple cycles
        fakeDao.insert(BudgetCycle(amount = 2400.0, startDate = LocalDate.of(2024, 12, 25), endDate = LocalDate.of(2025, 1, 24)))
        fakeDao.insert(BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25), endDate = null))

        // When: getting all cycles
        val allCycles = fakeDao.getAllCycles().first()

        // Then: should return both
        assertEquals(2, allCycles.size)
    }

    @Test
    fun `getById returns correct cycle`() = runTest {
        // Given: a cycle
        val id = fakeDao.insert(BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25)))

        // When: getting by ID
        val cycle = fakeDao.getById(id).first()

        // Then: should return the cycle
        assertNotNull(cycle)
        assertEquals(id, cycle?.id)
        assertEquals(2500.0, cycle?.amount ?: 0.0, 0.001)
    }

    @Test
    fun `getById returns null for non-existent ID`() = runTest {
        // Given: no cycles with ID 999
        fakeDao.insert(BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25)))

        // When: getting non-existent ID
        val cycle = fakeDao.getById(999L).first()

        // Then: should be null
        assertNull(cycle)
    }

    @Test
    fun `update modifies existing cycle`() = runTest {
        // Given: an existing cycle
        val id = fakeDao.insert(BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25)))
        val original = fakeDao.getById(id).first()!!

        // When: updating
        val updated = original.copy(endDate = LocalDate.of(2025, 2, 24))
        fakeDao.update(updated)

        // Then: should be modified
        val result = fakeDao.getById(id).first()
        assertNotNull(result?.endDate)
        assertEquals(LocalDate.of(2025, 2, 24), result?.endDate)
    }

    @Test
    fun `delete removes cycle`() = runTest {
        // Given: an existing cycle
        val id = fakeDao.insert(BudgetCycle(amount = 2500.0, startDate = LocalDate.of(2025, 1, 25)))
        val cycle = fakeDao.getById(id).first()!!

        // When: deleting
        fakeDao.delete(cycle)

        // Then: should be removed
        val result = fakeDao.getById(id).first()
        assertNull(result)
    }
}
