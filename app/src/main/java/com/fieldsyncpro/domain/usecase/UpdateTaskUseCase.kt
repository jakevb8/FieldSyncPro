package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.repository.TaskRepository
import javax.inject.Inject

/** Persists changes to an existing [FieldTask]. */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: FieldTask) = repository.updateTask(task)
}
