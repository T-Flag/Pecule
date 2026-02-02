package com.pecule.app.data.repository

import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.local.datastore.UserPreferences
import com.pecule.app.data.local.datastore.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepository(
    private val dataStore: UserPreferencesDataStore
) {
    val userPreferences: Flow<UserPreferences> = dataStore.userPreferences

    val isFirstLaunch: Flow<Boolean> = dataStore.isFirstLaunch

    suspend fun updateFirstName(firstName: String) {
        dataStore.updateFirstName(firstName)
    }

    suspend fun updateTheme(theme: ThemePreference) {
        dataStore.updateTheme(theme)
    }
}
