package com.fieldsyncpro.data.repository

import com.fieldsyncpro.data.local.dao.TaskDao
import com.fieldsyncpro.data.local.entity.TaskEntity
import com.fieldsyncpro.data.remote.api.TaskApiService
import com.fieldsyncpro.data.remote.dto.TaskDto
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TaskRepositoryImplTest {

    @MockK lateinit var taskDao: TaskDao
    @MockK lateinit var apiService: TaskApiService

    private lateinit var repository: TaskRepositoryImpl

    private val sampleEntity = TaskEntity(
        id          = "1",
        title       = "Test Task",
        description = "Description",
        status      = TaskStatus.PENDING,
        vibe        = "Steady",
        lastSynced  = 0L,
        isLocalOnly = false
    )

    private val sampleTask = FieldTask(
        id          = "1",
        title       = "Test Task",
        description = "Description",
        status      = TaskStatus.PENDING,
        vibe        = TaskVibe.Steady,
        lastSynced  = 0L,
        isLocalOnly = false
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        repository = TaskRepositoryImpl(taskDao, apiService)
    }

    // ── observeAllTasks ───────────────────────────────────────────────────────

    @Test
    fun `observeAllTasks maps entities to domain models`() = runTest {
        every { taskDao.observeAllTasks() } returns flowOf(listOf(sampleEntity))

        val tasks = repository.observeAllTasks().first()

        assertEquals(1, tasks.size)
        assertEquals(sampleEntity.id, tasks[0].id)
        assertEquals(sampleEntity.title, tasks[0].title)
        assertTrue(tasks[0].vibe is TaskVibe.Steady)
    }

    @Test
    fun `observeAllTasks returns empty list when dao is empty`() = runTest {
        every { taskDao.observeAllTasks() } returns flowOf(emptyList())

        val tasks = repository.observeAllTasks().first()
        assertTrue(tasks.isEmpty())
    }

    // ── observeTask ───────────────────────────────────────────────────────────

    @Test
    fun `observeTask returns domain model when entity exists`() = runTest {
        every { taskDao.observeTask("1") } returns flowOf(sampleEntity)

        val task = repository.observeTask("1").first()

        assertNotNull(task)
        assertEquals("1", task!!.id)
    }

    @Test
    fun `observeTask returns null when entity does not exist`() = runTest {
        every { taskDao.observeTask("999") } returns flowOf(null)

        val task = repository.observeTask("999").first()
        assertNull(task)
    }

    // ── createTask ────────────────────────────────────────────────────────────

    @Test
    fun `createTask inserts entity with isLocalOnly true and returns id`() = runTest {
        coEvery { taskDao.insertTask(any()) } just runs

        val id = repository.createTask(sampleTask)

        assertEquals(sampleTask.id, id)
        coVerify {
            taskDao.insertTask(withArg { entity ->
                assertTrue(entity.isLocalOnly)
                assertEquals(sampleTask.id, entity.id)
            })
        }
    }

    // ── updateTask ────────────────────────────────────────────────────────────

    @Test
    fun `updateTask calls dao updateTask`() = runTest {
        coEvery { taskDao.updateTask(any()) } just runs

        repository.updateTask(sampleTask)

        coVerify { taskDao.updateTask(any()) }
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    fun `deleteTask calls dao deleteTask with correct id`() = runTest {
        coEvery { taskDao.deleteTask("1") } just runs

        repository.deleteTask("1")

        coVerify { taskDao.deleteTask("1") }
    }

    // ── updateTaskStatus ──────────────────────────────────────────────────────

    @Test
    fun `updateTaskStatus calls dao with correct status string`() = runTest {
        coEvery { taskDao.updateTaskStatus(any(), any()) } just runs

        repository.updateTaskStatus("1", TaskStatus.COMPLETED)

        coVerify { taskDao.updateTaskStatus("1", "COMPLETED") }
    }

    // ── syncTasks ─────────────────────────────────────────────────────────────

    @Test
    fun `syncTasks returns true when api and dao succeed`() = runTest {
        val remoteDto = TaskDto("remote-1", "Remote Task", "desc", "PENDING", "Hype", 0L)
        coEvery { taskDao.getLocalOnlyTasks() } returns emptyList()
        coEvery { apiService.getTasks() } returns listOf(remoteDto)
        coEvery { taskDao.insertTasks(any()) } just runs

        val result = repository.syncTasks()

        assertTrue(result)
        coVerify { taskDao.insertTasks(any()) }
    }

    @Test
    fun `syncTasks returns false when api throws`() = runTest {
        coEvery { taskDao.getLocalOnlyTasks() } returns emptyList()
        coEvery { apiService.getTasks() } throws RuntimeException("Network error")

        val result = repository.syncTasks()

        assertFalse(result)
    }

    @Test
    fun `syncTasks pushes local-only tasks before pulling`() = runTest {
        val localEntity = sampleEntity.copy(id = "local-1", isLocalOnly = true)
        val createdDto  = TaskDto("local-1", "Test Task", "Description", "PENDING", "Steady", 0L)

        coEvery { taskDao.getLocalOnlyTasks() } returns listOf(localEntity)
        coEvery { taskDao.updateTaskStatus("local-1", "SYNCING") } just runs
        coEvery { apiService.createTask(any()) } returns createdDto
        coEvery { taskDao.insertTask(any()) } just runs
        coEvery { apiService.getTasks() } returns listOf(createdDto)
        coEvery { taskDao.insertTasks(any()) } just runs

        val result = repository.syncTasks()

        assertTrue(result)
        coVerify { apiService.createTask(any()) }
        coVerify { taskDao.updateTaskStatus("local-1", "SYNCING") }
    }

    @Test
    fun `syncTasks reverts to PENDING when createTask api call fails`() = runTest {
        val localEntity = sampleEntity.copy(id = "local-fail", isLocalOnly = true)

        coEvery { taskDao.getLocalOnlyTasks() } returns listOf(localEntity)
        coEvery { taskDao.updateTaskStatus("local-fail", "SYNCING") } just runs
        coEvery { apiService.createTask(any()) } throws RuntimeException("API down")
        coEvery { taskDao.updateTaskStatus("local-fail", "PENDING") } just runs
        coEvery { apiService.getTasks() } returns emptyList()
        coEvery { taskDao.insertTasks(any()) } just runs

        val result = repository.syncTasks()

        assertTrue(result) // outer sync still succeeds (partial)
        coVerify { taskDao.updateTaskStatus("local-fail", "PENDING") }
    }
}
