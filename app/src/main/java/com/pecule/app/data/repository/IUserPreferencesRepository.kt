package com.pecule.app.data.repository

import com.pecule.app.data.local.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    val isFirstLaunch: Flow<Boolean>
    suspend fun updateFirstName(firstName: String)
}
