package com.fieldsyncpro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.usecase.DeleteTaskUseCase
import com.fieldsyncpro.domain.usecase.GetAllTasksUseCase
import com.fieldsyncpro.domain.usecase.SyncTasksUseCase
import com.fieldsyncpro.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val syncTasksUseCase: SyncTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TaskListEffect>(Channel.BUFFERED)
    val effects: Flow<TaskListEffect> = _effects.receiveAsFlow()

    init {
        observeTasks()
    }

    fun onIntent(intent: TaskListIntent) {
        when (intent) {
            is TaskListIntent.LoadTasks         -> observeTasks()
            is TaskListIntent.TriggerSync       -> triggerSync()
            is TaskListIntent.DeleteTask        -> deleteTask(intent.id)
            is TaskListIntent.FilterByVibe      -> applyFilter(intent.filter)
            is TaskListIntent.CompleteTask      -> completeTask(intent.id)
        }
    }

    private fun observeTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAllTasksUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { tasks ->
                    val filter = _uiState.value.activeFilter
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = applyVibeFilter(tasks, filter),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val success = syncTasksUseCase()
            _uiState.update { it.copy(isSyncing = false) }
            if (success) {
                _effects.send(TaskListEffect.SyncCompleted)
                _effects.send(TaskListEffect.ShowSnackbar("Sync completed"))
            } else {
                _effects.send(TaskListEffect.SyncFailed)
                _effects.send(TaskListEffect.ShowSnackbar("Sync failed — working offline"))
            }
        }
    }

    private fun deleteTask(id: String) {
        viewModelScope.launch {
            runCatching { deleteTaskUseCase(id) }
                .onSuccess { _effects.send(TaskListEffect.ShowSnackbar("Task deleted")) }
                .onFailure { _effects.send(TaskListEffect.ShowSnackbar("Failed to delete task")) }
        }
    }

    private fun completeTask(id: String) {
        viewModelScope.launch {
            val task = _uiState.value.tasks.find { it.id == id } ?: return@launch
            runCatching {
                updateTaskUseCase(task.copy(status = TaskStatus.COMPLETED))
            }.onFailure {
                _effects.send(TaskListEffect.ShowSnackbar("Could not update task"))
            }
        }
    }

    private fun applyFilter(filter: TaskVibeFilter) {
        viewModelScope.launch {
            val allTasks = getAllTasksUseCase().first()
            _uiState.update {
                it.copy(
                    activeFilter = filter,
                    tasks = applyVibeFilter(allTasks, filter)
                )
            }
        }
    }

    private fun applyVibeFilter(
        tasks: List<com.fieldsyncpro.domain.model.FieldTask>,
        filter: TaskVibeFilter
    ) = when (filter) {
        TaskVibeFilter.ALL    -> tasks
        TaskVibeFilter.HYPE   -> tasks.filter { it.vibe is com.fieldsyncpro.domain.model.TaskVibe.Hype }
        TaskVibeFilter.STEADY -> tasks.filter { it.vibe is com.fieldsyncpro.domain.model.TaskVibe.Steady }
        TaskVibeFilter.CHILL  -> tasks.filter { it.vibe is com.fieldsyncpro.domain.model.TaskVibe.Chill }
    }
}
