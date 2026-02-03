package com.pecule.app.ui.screens.onboarding

import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.local.datastore.UserPreferences
import com.pecule.app.data.repository.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeUserPreferencesRepository : IUserPreferencesRepository {
    private val _userPreferences = MutableStateFlow(UserPreferences())

    override val userPreferences: Flow<UserPreferences> = _userPreferences

    override val isFirstLaunch: Flow<Boolean> = _userPreferences.map { it.firstName.isEmpty() }

    override suspend fun updateFirstName(firstName: String) {
        _userPreferences.value = _userPreferences.value.copy(firstName = firstName)
    }

    suspend fun updateTheme(theme: ThemePreference) {
        _userPreferences.value = _userPreferences.value.copy(theme = theme)
    }

    fun getCurrentFirstName(): String = _userPreferences.value.firstName
}
