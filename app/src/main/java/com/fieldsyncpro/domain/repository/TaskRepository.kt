package com.fieldsyncpro.domain.repository

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first repository contract for [FieldTask] data.
 *
 * All reads are reactive [Flow] streams backed by Room; writes apply
 * optimistically to the local DB and schedule a WorkManager sync.
 */
interface TaskRepository {

    /** Observe all tasks ordered by status then lastSynced descending. */
    fun observeAllTasks(): Flow<List<FieldTask>>

    /** Observe a single task by [id], or emit `null` if not found. */
    fun observeTask(id: String): Flow<FieldTask?>

    /**
     * Create a new task locally with [isLocalOnly] = `true`.
     * Returns the stable ID assigned to the task.
     */
    suspend fun createTask(task: FieldTask): String

    /** Update an existing task in the local store. */
    suspend fun updateTask(task: FieldTask)

    /** Delete a task by [id]. */
    suspend fun deleteTask(id: String)

    /**
     * Push any un-synced local tasks to the remote API, then pull
     * remote changes and merge into the local store.
     * Returns `true` if the full sync completed without errors.
     */
    suspend fun syncTasks(): Boolean

    /** Update only the [TaskStatus] for a task identified by [id]. */
    suspend fun updateTaskStatus(id: String, status: TaskStatus)
}
