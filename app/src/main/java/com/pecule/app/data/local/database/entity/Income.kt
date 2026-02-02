package com.pecule.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "incomes",
    foreignKeys = [
        ForeignKey(
            entity = BudgetCycle::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cycleId"])]
)
data class Income(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleId: Long,
    val label: String,
    val amount: Double,
    val date: LocalDate,
    val isFixed: Boolean = false
)
