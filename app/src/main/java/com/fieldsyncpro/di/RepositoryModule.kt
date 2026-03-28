package com.fieldsyncpro.di

import com.fieldsyncpro.data.local.dao.TaskDao
import com.fieldsyncpro.data.remote.api.TaskApiService
import com.fieldsyncpro.data.repository.TaskRepositoryImpl
import com.fieldsyncpro.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        apiService: TaskApiService
    ): TaskRepository = TaskRepositoryImpl(taskDao, apiService)
}
