package com.pecule.app.data.repository

import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.local.datastore.UserPreferences
import com.pecule.app.data.local.datastore.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: UserPreferencesDataStore
) : IUserPreferencesRepository {
    override val userPreferences: Flow<UserPreferences> = dataStore.userPreferences

    override val isFirstLaunch: Flow<Boolean> = dataStore.isFirstLaunch

    override suspend fun updateFirstName(firstName: String) {
        dataStore.updateFirstName(firstName)
    }

    override suspend fun updateTheme(theme: ThemePreference) {
        dataStore.updateTheme(theme)
    }
}
