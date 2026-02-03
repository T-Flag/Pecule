package com.pecule.app.ui.screens.onboarding

import java.time.LocalDate

data class OnboardingValidationResult(
    val isFirstNameValid: Boolean,
    val isAmountValid: Boolean,
    val isDateValid: Boolean
) {
    val isValid: Boolean = isFirstNameValid && isAmountValid && isDateValid
}

fun validateOnboardingInput(
    firstName: String,
    amount: Double?,
    date: LocalDate?,
    today: LocalDate = LocalDate.now()
): OnboardingValidationResult {
    return OnboardingValidationResult(
        isFirstNameValid = validateFirstName(firstName),
        isAmountValid = validateAmount(amount),
        isDateValid = validateDate(date, today)
    )
}

fun validateFirstName(firstName: String): Boolean {
    return firstName.isNotBlank()
}

fun validateAmount(amount: Double?): Boolean {
    return amount != null && amount > 0
}

fun validateDate(date: LocalDate?, today: LocalDate = LocalDate.now()): Boolean {
    return date != null && !date.isAfter(today)
}
