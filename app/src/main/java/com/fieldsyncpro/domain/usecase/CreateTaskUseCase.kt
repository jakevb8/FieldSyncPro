package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.repository.TaskRepository
import javax.inject.Inject

/** Creates a new [FieldTask] locally (optimistic) and returns its ID. */
class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: FieldTask): String = repository.createTask(task)
}
