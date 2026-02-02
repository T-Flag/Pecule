package com.pecule.app.di

import android.content.Context
import com.pecule.app.data.local.datastore.UserPreferencesDataStore
import com.pecule.app.data.local.datastore.userPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): UserPreferencesDataStore {
        return UserPreferencesDataStore(context.userPreferencesDataStore)
    }
}
