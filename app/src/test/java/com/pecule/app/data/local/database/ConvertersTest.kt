package com.pecule.app.data.local.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ConvertersTest {

    private lateinit var converters: Converters

    @Before
    fun setup() {
        converters = Converters()
    }

    @Test
    fun `LocalDate to Long and back should return same date`() {
        val originalDate = LocalDate.of(2024, 6, 15)

        val epochDay = converters.fromLocalDate(originalDate)
        val resultDate = converters.toLocalDate(epochDay)

        assertEquals(originalDate, resultDate)
    }

    @Test
    fun `null LocalDate should return null Long`() {
        val result = converters.fromLocalDate(null)
        assertNull(result)
    }

    @Test
    fun `null Long should return null LocalDate`() {
        val result = converters.toLocalDate(null)
        assertNull(result)
    }
}
