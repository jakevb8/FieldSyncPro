package com.fieldsyncpro.data.repository

import com.fieldsyncpro.data.local.dao.TaskDao
import com.fieldsyncpro.data.remote.api.TaskApiService
import com.fieldsyncpro.data.repository.TaskMapper.toDomain
import com.fieldsyncpro.data.repository.TaskMapper.toDto
import com.fieldsyncpro.data.repository.TaskMapper.toEntity
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline-first implementation of [TaskRepository].
 *
 * Strategy:
 * 1. All reads are served from the local Room database (single source of truth).
 * 2. Writes apply optimistically to Room first, then the repository marks them
 *    as [isLocalOnly] so WorkManager can push them when connectivity is available.
 * 3. [syncTasks] pushes local-only tasks upstream, then fetches the full remote
 *    list and merges it into Room (last-write-wins on conflicts).
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val apiService: TaskApiService
) : TaskRepository {

    override fun observeAllTasks(): Flow<List<FieldTask>> =
        taskDao.observeAllTasks().map { entities -> entities.map { it.toDomain() } }

    override fun observeTask(id: String): Flow<FieldTask?> =
        taskDao.observeTask(id).map { it?.toDomain() }

    override suspend fun createTask(task: FieldTask): String {
        taskDao.insertTask(task.copy(isLocalOnly = true).toEntity())
        return task.id
    }

    override suspend fun updateTask(task: FieldTask) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(id: String) {
        taskDao.deleteTask(id)
    }

    override suspend fun updateTaskStatus(id: String, status: TaskStatus) {
        taskDao.updateTaskStatus(id, status.name)
    }

    override suspend fun syncTasks(): Boolean {
        return try {
            // 1. Push any local-only tasks to the server
            val localOnly = taskDao.getLocalOnlyTasks()
            for (entity in localOnly) {
                val domain = entity.toDomain()
                taskDao.updateTaskStatus(entity.id, TaskStatus.SYNCING.name)
                try {
                    val created = apiService.createTask(domain.toDto())
                    taskDao.insertTask(
                        created.toEntity().copy(isLocalOnly = false)
                    )
                } catch (e: Exception) {
                    // Revert SYNCING → PENDING on failure
                    taskDao.updateTaskStatus(entity.id, TaskStatus.PENDING.name)
                }
            }

            // 2. Pull remote tasks and merge into local store
            val remoteTasks = apiService.getTasks()
            val remoteEntities = remoteTasks.map { dto ->
                dto.toEntity().copy(isLocalOnly = false)
            }
            taskDao.insertTasks(remoteEntities)

            true
        } catch (e: Exception) {
            false
        }
    }
}
