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
                // 1. Create categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        isDefault INTEGER NOT NULL
                    )
                """.trimIndent())

                // 2. Insert default categories with their IDs
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (1, 'Salaire', 'payments', ${0xFF4CAF50L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (2, 'Alimentation', 'restaurant', ${0xFFFF9800L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (3, 'Transport', 'directions_car', ${0xFF2196F3L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (4, 'Logement', 'home', ${0xFF9C27B0L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (5, 'Factures', 'receipt_long', ${0xFFFFEB3BL}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (6, 'Loisirs', 'sports_esports', ${0xFFE91E63L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (7, 'Santé', 'medical_services', ${0xFF00BCD4L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (8, 'Shopping', 'shopping_bag', ${0xFFFF5722L}, 1)")
                database.execSQL("INSERT INTO categories (id, name, icon, color, isDefault) VALUES (9, 'Autre', 'more_horiz', ${0xFF607D8BL}, 1)")

                // 3. Create new expenses table with categoryId
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cycleId INTEGER NOT NULL,
                        categoryId INTEGER,
                        label TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date INTEGER NOT NULL,
                        isFixed INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY (cycleId) REFERENCES budget_cycles(id) ON DELETE CASCADE,
                        FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL
                    )
                """.trimIndent())

                // 4. Copy data from old expenses with category mapping
                database.execSQL("""
                    INSERT INTO expenses_new (id, cycleId, categoryId, label, amount, date, isFixed)
                    SELECT id, cycleId,
                        CASE category
                            WHEN 'SALARY' THEN 1
                            WHEN 'FOOD' THEN 2
                            WHEN 'TRANSPORT' THEN 3
                            WHEN 'HOUSING' THEN 4
                            WHEN 'UTILITIES' THEN 5
                            WHEN 'ENTERTAINMENT' THEN 6
                            WHEN 'HEALTH' THEN 7
                            WHEN 'SHOPPING' THEN 8
                            WHEN 'OTHER' THEN 9
                            ELSE 9
                        END,
                        label, amount, date, isFixed
                    FROM expenses
                """.trimIndent())

                // 5. Drop old table and rename new one
                database.execSQL("DROP TABLE expenses")
                database.execSQL("ALTER TABLE expenses_new RENAME TO expenses")

                // 6. Create indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_cycleId ON expenses(cycleId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses(categoryId)")
            }
        }
    }
}
