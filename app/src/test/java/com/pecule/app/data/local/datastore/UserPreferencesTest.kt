package com.pecule.app.data.local.datastore

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun `UserPreferences has correct default values`() {
        val preferences = UserPreferences()

        assertEquals("", preferences.firstName)
        assertEquals(ThemePreference.AUTO, preferences.theme)
    }

    @Test
    fun `UserPreferences stores custom values correctly`() {
        val preferences = UserPreferences(
            firstName = "Thomas",
            theme = ThemePreference.DARK
        )

        assertEquals("Thomas", preferences.firstName)
        assertEquals(ThemePreference.DARK, preferences.theme)
    }

    @Test
    fun `ThemePreference valueOf returns correct enum`() {
        assertEquals(ThemePreference.AUTO, ThemePreference.valueOf("AUTO"))
        assertEquals(ThemePreference.LIGHT, ThemePreference.valueOf("LIGHT"))
        assertEquals(ThemePreference.DARK, ThemePreference.valueOf("DARK"))
    }

    @Test
    fun `ThemePreference name returns correct string`() {
        assertEquals("AUTO", ThemePreference.AUTO.name)
        assertEquals("LIGHT", ThemePreference.LIGHT.name)
        assertEquals("DARK", ThemePreference.DARK.name)
    }
}
