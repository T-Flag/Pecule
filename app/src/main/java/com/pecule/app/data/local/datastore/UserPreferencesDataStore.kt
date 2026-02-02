package com.pecule.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension pour créer le DataStore
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    // Clés de stockage
    private object PreferencesKeys {
        val FIRST_NAME = stringPreferencesKey("first_name")
        val THEME = stringPreferencesKey("theme")
    }

    // Lecture des préférences (Flow réactif)
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { preferences ->
        UserPreferences(
            firstName = preferences[PreferencesKeys.FIRST_NAME] ?: "",
            theme = preferences[PreferencesKeys.THEME]?.let {
                ThemePreference.valueOf(it)
            } ?: ThemePreference.AUTO
        )
    }

    // Mise à jour du prénom
    suspend fun updateFirstName(firstName: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_NAME] = firstName
        }
    }

    // Mise à jour du thème
    suspend fun updateTheme(theme: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    // Vérifie si c'est le premier lancement (prénom vide)
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FIRST_NAME].isNullOrEmpty()
    }
}
