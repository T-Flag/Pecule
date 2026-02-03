package com.pecule.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.datastore.ThemePreference
import com.pecule.app.data.repository.IUserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: IUserPreferencesRepository
) : ViewModel() {

    val firstName: StateFlow<String> = userPreferencesRepository.userPreferences
        .map { it.firstName }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val theme: StateFlow<ThemePreference> = userPreferencesRepository.userPreferences
        .map { it.theme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.AUTO
        )

    fun updateFirstName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            userPreferencesRepository.updateFirstName(name)
        }
    }

    fun updateTheme(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferencesRepository.updateTheme(theme)
        }
    }
}
