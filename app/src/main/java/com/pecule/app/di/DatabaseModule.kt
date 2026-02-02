package com.pecule.app.di

import android.content.Context
import androidx.room.Room
import com.pecule.app.data.local.database.PeculeDatabase
import com.pecule.app.data.local.database.dao.BudgetCycleDao
import com.pecule.app.data.local.database.dao.ExpenseDao
import com.pecule.app.data.local.database.dao.IncomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PeculeDatabase {
        return Room.databaseBuilder(
            context,
            PeculeDatabase::class.java,
            "pecule_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBudgetCycleDao(database: PeculeDatabase): BudgetCycleDao {
        return database.budgetCycleDao()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: PeculeDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideIncomeDao(database: PeculeDatabase): IncomeDao {
        return database.incomeDao()
    }
}
