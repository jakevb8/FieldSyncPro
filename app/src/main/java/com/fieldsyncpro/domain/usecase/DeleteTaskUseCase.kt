package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.repository.TaskRepository
import javax.inject.Inject

/** Deletes a task by its ID from the local store. */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteTask(id)
}
