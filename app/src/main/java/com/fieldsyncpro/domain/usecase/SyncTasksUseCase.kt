package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.repository.TaskRepository
import javax.inject.Inject

/** Triggers an offline-first sync cycle against the remote API. */
class SyncTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(): Boolean = repository.syncTasks()
}
