package com.pecule.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.repository.BudgetCycleRepository
import com.pecule.app.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val budgetCycleRepository: BudgetCycleRepository
) : ViewModel() {

    val isFirstLaunch: StateFlow<Boolean> = userPreferencesRepository.isFirstLaunch
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun completeOnboarding(firstName: String, salaryAmount: Double, salaryDate: LocalDate) {
        viewModelScope.launch {
            userPreferencesRepository.updateFirstName(firstName)
            budgetCycleRepository.insert(
                BudgetCycle(
                    amount = salaryAmount,
                    startDate = salaryDate,
                    endDate = null
                )
            )
        }
    }
}
