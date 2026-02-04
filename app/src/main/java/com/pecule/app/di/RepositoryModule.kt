package com.pecule.app.di

import com.pecule.app.data.repository.BudgetCycleRepository
import com.pecule.app.data.repository.CategoryRepository
import com.pecule.app.data.repository.ExpenseRepository
import com.pecule.app.data.repository.IBudgetCycleRepository
import com.pecule.app.data.repository.ICategoryRepository
import com.pecule.app.data.repository.IExpenseRepository
import com.pecule.app.data.repository.IIncomeRepository
import com.pecule.app.data.repository.IUserPreferencesRepository
import com.pecule.app.data.repository.IncomeRepository
import com.pecule.app.data.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepository
    ): IUserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindBudgetCycleRepository(
        impl: BudgetCycleRepository
    ): IBudgetCycleRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepository
    ): IExpenseRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(
        impl: IncomeRepository
    ): IIncomeRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepository
    ): ICategoryRepository
}
