package com.fieldsyncpro.presentation.viewmodel

import app.cash.turbine.test
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.domain.repository.TaskRepository
import com.fieldsyncpro.domain.usecase.CreateTaskUseCase
import com.fieldsyncpro.domain.usecase.DeleteTaskUseCase
import com.fieldsyncpro.domain.usecase.UpdateTaskUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModelTest {

    @MockK lateinit var taskRepository: TaskRepository
    @MockK lateinit var createTaskUseCase: CreateTaskUseCase
    @MockK lateinit var updateTaskUseCase: UpdateTaskUseCase
    @MockK lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleTask = FieldTask(
        id          = "task-42",
        title       = "Inspect Generator",
        description = "Annual check",
        status      = TaskStatus.PENDING,
        vibe        = TaskVibe.Steady,
        lastSynced  = 0L,
        isLocalOnly = false
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun createViewModel() = TaskDetailViewModel(
        taskRepository, createTaskUseCase, updateTaskUseCase, deleteTaskUseCase
    )

    // ── LoadTask ──────────────────────────────────────────────────────────────

    @Test
    fun `LoadTask populates uiState with the task`() = runTest {
        every { taskRepository.observeTask("task-42") } returns flowOf(sampleTask)
        val viewModel = createViewModel()

        viewModel.onIntent(TaskDetailIntent.LoadTask("task-42"))

        assertEquals(sampleTask, viewModel.uiState.value.task)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `LoadTask with null result sets task to null`() = runTest {
        every { taskRepository.observeTask("missing") } returns flowOf(null)
        val viewModel = createViewModel()

        viewModel.onIntent(TaskDetailIntent.LoadTask("missing"))

        assertNull(viewModel.uiState.value.task)
    }

    // ── initNewTask ───────────────────────────────────────────────────────────

    @Test
    fun `initNewTask sets task in state`() {
        val viewModel = createViewModel()
        val newTask = sampleTask.copy(id = "new-1", isLocalOnly = true)

        viewModel.initNewTask(newTask)

        assertEquals(newTask, viewModel.uiState.value.task)
    }

    // ── UpdateTitle / UpdateDescription / UpdateVibe / UpdateStatus ───────────

    @Test
    fun `UpdateTitle modifies task title in state`() {
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.onIntent(TaskDetailIntent.UpdateTitle("New Title"))

        assertEquals("New Title", viewModel.uiState.value.task?.title)
    }

    @Test
    fun `UpdateDescription modifies task description in state`() {
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.onIntent(TaskDetailIntent.UpdateDescription("New Desc"))

        assertEquals("New Desc", viewModel.uiState.value.task?.description)
    }

    @Test
    fun `UpdateVibe modifies task vibe in state`() {
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.onIntent(TaskDetailIntent.UpdateVibe(TaskVibe.Hype))

        assertTrue(viewModel.uiState.value.task?.vibe is TaskVibe.Hype)
    }

    @Test
    fun `UpdateStatus modifies task status in state`() {
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.onIntent(TaskDetailIntent.UpdateStatus(TaskStatus.COMPLETED))

        assertEquals(TaskStatus.COMPLETED, viewModel.uiState.value.task?.status)
    }

    // ── SaveTask — create path ────────────────────────────────────────────────

    @Test
    fun `SaveTask calls createTaskUseCase for local-only task`() = runTest {
        coEvery { createTaskUseCase(any()) } returns "new-1"
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask.copy(isLocalOnly = true))

        viewModel.effects.test {
            viewModel.onIntent(TaskDetailIntent.SaveTask)
            val effects = listOf(awaitItem(), awaitItem())
            assertTrue(effects.any { it is TaskDetailEffect.ShowSnackbar })
            assertTrue(effects.any { it is TaskDetailEffect.NavigateBack })
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { createTaskUseCase(any()) }
    }

    // ── SaveTask — update path ────────────────────────────────────────────────

    @Test
    fun `SaveTask calls updateTaskUseCase for existing task`() = runTest {
        coEvery { updateTaskUseCase(any()) } just runs
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask.copy(isLocalOnly = false))

        viewModel.effects.test {
            viewModel.onIntent(TaskDetailIntent.SaveTask)
            val effects = listOf(awaitItem(), awaitItem())
            assertTrue(effects.any { it is TaskDetailEffect.NavigateBack })
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { updateTaskUseCase(any()) }
    }

    @Test
    fun `SaveTask emits error snackbar on exception`() = runTest {
        coEvery { updateTaskUseCase(any()) } throws RuntimeException("Save failed")
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask.copy(isLocalOnly = false))

        viewModel.effects.test {
            viewModel.onIntent(TaskDetailIntent.SaveTask)
            val effect = awaitItem()
            assertTrue(effect is TaskDetailEffect.ShowSnackbar)
            assertTrue((effect as TaskDetailEffect.ShowSnackbar).message.contains("Failed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveTask does nothing when task is null`() = runTest {
        val viewModel = createViewModel()
        // No task initialised
        viewModel.onIntent(TaskDetailIntent.SaveTask)

        coVerify(exactly = 0) { createTaskUseCase(any()) }
        coVerify(exactly = 0) { updateTaskUseCase(any()) }
    }

    // ── DeleteTask ────────────────────────────────────────────────────────────

    @Test
    fun `DeleteTask emits TaskDeleted and NavigateBack on success`() = runTest {
        coEvery { deleteTaskUseCase(any()) } just runs
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.effects.test {
            viewModel.onIntent(TaskDetailIntent.DeleteTask)
            val effects = listOf(awaitItem(), awaitItem())
            assertTrue(effects.any { it is TaskDetailEffect.TaskDeleted })
            assertTrue(effects.any { it is TaskDetailEffect.NavigateBack })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteTask emits error snackbar on exception`() = runTest {
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("Delete failed")
        val viewModel = createViewModel()
        viewModel.initNewTask(sampleTask)

        viewModel.effects.test {
            viewModel.onIntent(TaskDetailIntent.DeleteTask)
            val effect = awaitItem()
            assertTrue(effect is TaskDetailEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
