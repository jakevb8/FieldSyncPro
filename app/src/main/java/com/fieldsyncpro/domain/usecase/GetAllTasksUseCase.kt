package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Returns a reactive stream of all [FieldTask] objects. */
class GetAllTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<FieldTask>> = repository.observeAllTasks()
}
