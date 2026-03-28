package com.fieldsyncpro.data.local.dao

import androidx.room.*
import com.fieldsyncpro.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/** Data Access Object for [TaskEntity] CRUD and observation. */
@Dao
interface TaskDao {

    // ── Queries ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM field_tasks ORDER BY status ASC, last_synced DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM field_tasks WHERE id = :id")
    fun observeTask(id: String): Flow<TaskEntity?>

    @Query("SELECT * FROM field_tasks WHERE is_local_only = 1")
    suspend fun getLocalOnlyTasks(): List<TaskEntity>

    @Query("SELECT * FROM field_tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    // ── Writes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM field_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM field_tasks")
    suspend fun deleteAllTasks()

    @Query("UPDATE field_tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)
}
