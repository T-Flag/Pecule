package com.pecule.app.domain

import com.pecule.app.data.local.database.entity.Category
import java.time.LocalDate

data class Transaction(
    val id: Long,
    val label: String,
    val amount: Double,
    val date: LocalDate,
    val isExpense: Boolean,
    val isFixed: Boolean,
    val category: Category?
)
