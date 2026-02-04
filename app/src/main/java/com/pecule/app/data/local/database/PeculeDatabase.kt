package com.pecule.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pecule.app.data.local.database.dao.BudgetCycleDao
import com.pecule.app.data.local.database.dao.CategoryDao
import com.pecule.app.data.local.database.dao.ExpenseDao
import com.pecule.app.data.local.database.dao.IncomeDao
import com.pecule.app.data.local.database.entity.BudgetCycle
import com.pecule.app.data.local.database.entity.CategoryEntity
import com.pecule.app.data.local.database.entity.Expense
import com.pecule.app.data.local.database.entity.Income

@Database(
    entities = [BudgetCycle::class, Expense::class, Income::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PeculeDatabase : RoomDatabase() {
    abstract fun budgetCycleDao(): BudgetCycleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
    }
}
