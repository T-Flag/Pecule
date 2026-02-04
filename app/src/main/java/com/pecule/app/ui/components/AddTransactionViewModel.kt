package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.domain.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class AddTransactionUiState(
    val label: String = "",
    val amount: Double? = null,
    val category: CategoryEntity? = null,
    val date: LocalDate = LocalDate.now(),
    val isFixed: Boolean = false,
    val isExpense: Boolean = true,
    val isEditing: Boolean = false,
    val transactionId: Long? = null
)

data class SaveResult(
    val isSuccess: Boolean,
    val errors: List<String> = emptyList()
)

class AddTransactionViewModel(
    private val expenseRepository: IExpenseRepository,
    private val incomeRepository: IIncomeRepository,
    isExpense: Boolean,
    private val cycleId: Long,
    existingTransaction: Transaction?
) {
    private val _uiState = MutableStateFlow(
        if (existingTransaction != null) {
            AddTransactionUiState(
                label = existingTransaction.label,
                amount = existingTransaction.amount,
                category = existingTransaction.category,
                date = existingTransaction.date,
                isFixed = existingTransaction.isFixed,
                isExpense = isExpense,
                isEditing = true,
                transactionId = existingTransaction.id
            )
        } else {
            AddTransactionUiState(
                isExpense = isExpense
            )
        }
    )

    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun updateLabel(label: String) {
        _uiState.update { it.copy(label = label) }
    }

    fun updateAmount(amount: Double?) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateCategory(category: CategoryEntity?) {
        // Ignore category update for income
        if (!_uiState.value.isExpense) return
        _uiState.update { it.copy(category = category) }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun toggleIsFixed() {
        _uiState.update { it.copy(isFixed = !it.isFixed) }
    }

    suspend fun save(): SaveResult {
        val state = _uiState.value

        val errors = validateTransaction(
            label = state.label,
            amount = state.amount,
            category = state.category,
            date = state.date,
            isExpense = state.isExpense
        )

        if (errors.isNotEmpty()) {
            return SaveResult(isSuccess = false, errors = errors)
        }

        if (state.isExpense) {
            saveExpense(state)
        } else {
            saveIncome(state)
        }

        return SaveResult(isSuccess = true)
    }

    private suspend fun saveExpense(state: AddTransactionUiState) {
        val expense = Expense(
            id = state.transactionId ?: 0L,
            cycleId = cycleId,
            categoryId = state.category?.id,
            label = state.label,
            amount = state.amount!!,
            date = state.date,
            isFixed = state.isFixed
        )

        if (state.isEditing) {
            expenseRepository.update(expense)
        } else {
            expenseRepository.insert(expense)
        }
    }

    private suspend fun saveIncome(state: AddTransactionUiState) {
        val income = Income(
            id = state.transactionId ?: 0L,
            cycleId = cycleId,
            label = state.label,
            amount = state.amount!!,
            date = state.date,
            isFixed = state.isFixed
        )

        if (state.isEditing) {
            incomeRepository.update(income)
        } else {
            incomeRepository.insert(income)
        }
    }
}
