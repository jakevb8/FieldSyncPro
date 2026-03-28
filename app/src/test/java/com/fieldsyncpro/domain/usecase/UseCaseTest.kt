package com.fieldsyncpro.domain.usecase

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.domain.repository.TaskRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UseCaseTest {

    @MockK lateinit var repository: TaskRepository

    private val task = FieldTask(
        id          = "uc-1",
        title       = "Use Case Task",
        description = "Test",
        status      = TaskStatus.PENDING,
        vibe        = TaskVibe.Hype,
        lastSynced  = 0L,
        isLocalOnly = true
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `GetAllTasksUseCase returns flow from repository`() = runTest {
        every { repository.observeAllTasks() } returns flowOf(listOf(task))

        val result = GetAllTasksUseCase(repository)().first()

        assertEquals(listOf(task), result)
    }

    @Test
    fun `CreateTaskUseCase delegates to repository`() = runTest {
        coEvery { repository.createTask(task) } returns "uc-1"

        val id = CreateTaskUseCase(repository)(task)

        assertEquals("uc-1", id)
        coVerify { repository.createTask(task) }
    }

    @Test
    fun `UpdateTaskUseCase delegates to repository`() = runTest {
        coEvery { repository.updateTask(task) } just runs

        UpdateTaskUseCase(repository)(task)

        coVerify { repository.updateTask(task) }
    }

    @Test
    fun `DeleteTaskUseCase delegates to repository`() = runTest {
        coEvery { repository.deleteTask("uc-1") } just runs

        DeleteTaskUseCase(repository)("uc-1")

        coVerify { repository.deleteTask("uc-1") }
    }

    @Test
    fun `SyncTasksUseCase returns true on success`() = runTest {
        coEvery { repository.syncTasks() } returns true

        val result = SyncTasksUseCase(repository)()

        assertTrue(result)
    }

    @Test
    fun `SyncTasksUseCase returns false on failure`() = runTest {
        coEvery { repository.syncTasks() } returns false

        val result = SyncTasksUseCase(repository)()

        assertFalse(result)
    }
}
