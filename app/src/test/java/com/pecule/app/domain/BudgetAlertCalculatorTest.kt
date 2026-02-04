package com.pecule.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetAlertCalculatorTest {

    private val calculator = BudgetAlertCalculator()

    @Test
    fun `budget used 0 percent returns NONE`() {
        val result = calculator.calculate(0.0)
        assertEquals(BudgetAlertLevel.NONE, result.level)
    }

    @Test
    fun `budget used 25 percent returns NONE`() {
        val result = calculator.calculate(25.0)
        assertEquals(BudgetAlertLevel.NONE, result.level)
    }

    @Test
    fun `budget used 49 percent returns NONE`() {
        val result = calculator.calculate(49.0)
        assertEquals(BudgetAlertLevel.NONE, result.level)
    }

    @Test
    fun `budget used 50 percent returns WARNING`() {
        val result = calculator.calculate(50.0)
        assertEquals(BudgetAlertLevel.WARNING, result.level)
    }

    @Test
    fun `budget used 65 percent returns WARNING`() {
        val result = calculator.calculate(65.0)
        assertEquals(BudgetAlertLevel.WARNING, result.level)
    }

    @Test
    fun `budget used 79 percent returns WARNING`() {
        val result = calculator.calculate(79.0)
        assertEquals(BudgetAlertLevel.WARNING, result.level)
    }

    @Test
    fun `budget used 80 percent returns DANGER`() {
        val result = calculator.calculate(80.0)
        assertEquals(BudgetAlertLevel.DANGER, result.level)
    }

    @Test
    fun `budget used 90 percent returns DANGER`() {
        val result = calculator.calculate(90.0)
        assertEquals(BudgetAlertLevel.DANGER, result.level)
    }

    @Test
    fun `budget used 99 percent returns DANGER`() {
        val result = calculator.calculate(99.0)
        assertEquals(BudgetAlertLevel.DANGER, result.level)
    }

    @Test
    fun `budget used 100 percent returns EXCEEDED`() {
        val result = calculator.calculate(100.0)
        assertEquals(BudgetAlertLevel.EXCEEDED, result.level)
    }

    @Test
    fun `budget used 150 percent returns EXCEEDED`() {
        val result = calculator.calculate(150.0)
        assertEquals(BudgetAlertLevel.EXCEEDED, result.level)
    }

    @Test
    fun `WARNING alert contains percentage in message`() {
        val result = calculator.calculate(60.0)
        assertEquals("Attention, vous avez utilisé 60% de votre budget", result.message)
    }

    @Test
    fun `DANGER alert contains percentage in message`() {
        val result = calculator.calculate(85.0)
        assertEquals("Budget critique : 85% utilisé", result.message)
    }

    @Test
    fun `EXCEEDED alert contains percentage in message`() {
        val result = calculator.calculate(120.0)
        assertEquals("Budget dépassé ! 120% utilisé", result.message)
    }

    @Test
    fun `NONE alert has empty message`() {
        val result = calculator.calculate(30.0)
        assertEquals("", result.message)
    }

    @Test
    fun `negative percentage returns NONE`() {
        val result = calculator.calculate(-10.0)
        assertEquals(BudgetAlertLevel.NONE, result.level)
    }
}
