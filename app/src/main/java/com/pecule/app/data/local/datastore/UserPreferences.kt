package com.pecule.app.data.local.datastore

data class UserPreferences(
    val firstName: String = "",
    val theme: ThemePreference = ThemePreference.AUTO
)
