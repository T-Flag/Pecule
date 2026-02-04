package com.pecule.app.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pecule.app.domain.CycleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class NewSalaryState {
    data object Idle : NewSalaryState()
    data object Loading : NewSalaryState()
    data object Success : NewSalaryState()
    data class Error(val message: String) : NewSalaryState()
}

@HiltViewModel
class NewSalaryViewModel @Inject constructor(
    private val cycleManager: CycleManager
) : ViewModel() {

    private val _state = MutableStateFlow<NewSalaryState>(NewSalaryState.Idle)
    val state: StateFlow<NewSalaryState> = _state.asStateFlow()

    fun createNewCycle(amount: Double, startDate: LocalDate) {
        viewModelScope.launch {
            _state.value = NewSalaryState.Loading
            try {
                cycleManager.createNewCycle(amount, startDate)
                _state.value = NewSalaryState.Success
            } catch (e: Exception) {
                _state.value = NewSalaryState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun resetState() {
        _state.value = NewSalaryState.Idle
    }
}
