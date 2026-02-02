package com.pecule.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "budget_cycles")
data class BudgetCycle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val startDate: LocalDate,
    val endDate: LocalDate? = null  // null = cycle ouvert
)
