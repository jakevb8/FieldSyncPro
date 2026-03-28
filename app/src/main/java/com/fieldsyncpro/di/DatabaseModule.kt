package com.fieldsyncpro.di

import android.content.Context
import androidx.room.Room
import com.fieldsyncpro.data.local.FieldSyncDatabase
import com.fieldsyncpro.data.local.dao.TaskDao
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
    fun provideDatabase(@ApplicationContext context: Context): FieldSyncDatabase =
        Room.databaseBuilder(
            context,
            FieldSyncDatabase::class.java,
            FieldSyncDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(database: FieldSyncDatabase): TaskDao = database.taskDao()
}
