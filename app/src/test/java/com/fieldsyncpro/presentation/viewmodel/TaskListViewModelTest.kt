package com.fieldsyncpro.presentation.viewmodel

import app.cash.turbine.test
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.domain.usecase.DeleteTaskUseCase
import com.fieldsyncpro.domain.usecase.GetAllTasksUseCase
import com.fieldsyncpro.domain.usecase.SyncTasksUseCase
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
class TaskListViewModelTest {

    @MockK lateinit var getAllTasksUseCase: GetAllTasksUseCase
    @MockK lateinit var syncTasksUseCase: SyncTasksUseCase
    @MockK lateinit var deleteTaskUseCase: DeleteTaskUseCase
    @MockK lateinit var updateTaskUseCase: UpdateTaskUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleTasks = listOf(
        FieldTask("1", "Task A", "Desc A", TaskStatus.PENDING,   TaskVibe.Hype,   0L, false),
        FieldTask("2", "Task B", "Desc B", TaskStatus.COMPLETED, TaskVibe.Steady, 0L, false),
        FieldTask("3", "Task C", "Desc C", TaskStatus.PENDING,   TaskVibe.Chill,  0L, true)
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
        every { getAllTasksUseCase() } returns flowOf(sampleTasks)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun createViewModel() = TaskListViewModel(
        getAllTasksUseCase, syncTasksUseCase, deleteTaskUseCase, updateTaskUseCase
    )

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun `initial state has tasks loaded`() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.value
        assertEquals(sampleTasks, state.tasks)
        assertFalse(state.isLoading)
    }

    // ── TriggerSync ───────────────────────────────────────────────────────────

    @Test
    fun `TriggerSync emits SyncCompleted effect on success`() = runTest {
        coEvery { syncTasksUseCase() } returns true
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(TaskListIntent.TriggerSync)
            val effects = listOf(awaitItem(), awaitItem())
            assertTrue(effects.any { it is TaskListEffect.SyncCompleted })
            assertTrue(effects.any { it is TaskListEffect.ShowSnackbar && it.message == "Sync completed" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TriggerSync emits SyncFailed effect on failure`() = runTest {
        coEvery { syncTasksUseCase() } returns false
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(TaskListIntent.TriggerSync)
            val effects = listOf(awaitItem(), awaitItem())
            assertTrue(effects.any { it is TaskListEffect.SyncFailed })
            assertTrue(effects.any { it is TaskListEffect.ShowSnackbar })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TriggerSync sets isSyncing to false after completion`() = runTest {
        coEvery { syncTasksUseCase() } returns true
        val viewModel = createViewModel()

        viewModel.onIntent(TaskListIntent.TriggerSync)

        assertFalse(viewModel.uiState.value.isSyncing)
    }

    // ── DeleteTask ────────────────────────────────────────────────────────────

    @Test
    fun `DeleteTask emits ShowSnackbar on success`() = runTest {
        coEvery { deleteTaskUseCase(any()) } just runs
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(TaskListIntent.DeleteTask("1"))
            val effect = awaitItem()
            assertTrue(effect is TaskListEffect.ShowSnackbar)
            assertEquals("Task deleted", (effect as TaskListEffect.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteTask emits error snackbar on failure`() = runTest {
        coEvery { deleteTaskUseCase(any()) } throws RuntimeException("DB error")
        val viewModel = createViewModel()

        viewModel.effects.test {
            viewModel.onIntent(TaskListIntent.DeleteTask("1"))
            val effect = awaitItem()
            assertTrue(effect is TaskListEffect.ShowSnackbar)
            assertTrue((effect as TaskListEffect.ShowSnackbar).message.contains("Failed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── CompleteTask ──────────────────────────────────────────────────────────

    @Test
    fun `CompleteTask updates task status to COMPLETED`() = runTest {
        coEvery { updateTaskUseCase(any()) } just runs
        val viewModel = createViewModel()

        viewModel.onIntent(TaskListIntent.CompleteTask("1"))

        coVerify {
            updateTaskUseCase(withArg { task ->
                assertEquals("1", task.id)
                assertEquals(TaskStatus.COMPLETED, task.status)
            })
        }
    }

    @Test
    fun `CompleteTask does nothing for unknown task id`() = runTest {
        val viewModel = createViewModel()

        viewModel.onIntent(TaskListIntent.CompleteTask("unknown-id"))

        coVerify(exactly = 0) { updateTaskUseCase(any()) }
    }

    // ── FilterByVibe ──────────────────────────────────────────────────────────

    @Test
    fun `FilterByVibe HYPE shows only Hype tasks`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(TaskListIntent.FilterByVibe(TaskVibeFilter.HYPE))

        val tasks = viewModel.uiState.value.tasks
        assertTrue(tasks.all { it.vibe is TaskVibe.Hype })
        assertEquals(1, tasks.size)
    }

    @Test
    fun `FilterByVibe ALL shows all tasks`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(TaskListIntent.FilterByVibe(TaskVibeFilter.ALL))

        assertEquals(sampleTasks.size, viewModel.uiState.value.tasks.size)
    }

    @Test
    fun `FilterByVibe STEADY shows only Steady tasks`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(TaskListIntent.FilterByVibe(TaskVibeFilter.STEADY))

        val tasks = viewModel.uiState.value.tasks
        assertTrue(tasks.all { it.vibe is TaskVibe.Steady })
    }

    @Test
    fun `FilterByVibe CHILL shows only Chill tasks`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIntent(TaskListIntent.FilterByVibe(TaskVibeFilter.CHILL))

        val tasks = viewModel.uiState.value.tasks
        assertTrue(tasks.all { it.vibe is TaskVibe.Chill })
    }
}
