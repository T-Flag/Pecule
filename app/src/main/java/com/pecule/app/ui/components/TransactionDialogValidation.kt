package com.pecule.app.ui.components

import com.pecule.app.data.local.database.entity.CategoryEntity
import java.time.LocalDate

fun validateTransaction(
    label: String,
    amount: Double?,
    category: CategoryEntity?,
    date: LocalDate?,
    isExpense: Boolean
): List<String> {
    val errors = mutableListOf<String>()

    // Validate label
    if (label.isBlank()) {
        errors.add("Le libellé est requis")
    }

    // Validate amount
    when {
        amount == null -> errors.add("Le montant est requis")
        amount <= 0 -> errors.add("Le montant doit être supérieur à 0")
    }

    // Validate category (only for expenses)
    if (isExpense && category == null) {
        errors.add("La catégorie est requise")
    }

    // Validate date
    if (date == null) {
        errors.add("La date est requise")
    }

    return errors
}
