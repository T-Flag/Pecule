package com.pecule.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pecule.app.data.local.database.dao.BudgetCycleDao
import com.pecule.app.data.local.database.dao.ExpenseDao
import com.pecule.app.data.local.database.dao.IncomeDao
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income

@Database(
    entities = [BudgetCycle::class, Expense::class, Income::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PeculeDatabase : RoomDatabase() {
    abstract fun budgetCycleDao(): BudgetCycleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
}
