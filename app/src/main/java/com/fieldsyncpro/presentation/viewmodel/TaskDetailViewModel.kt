package com.fieldsyncpro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.domain.repository.TaskRepository
import com.fieldsyncpro.domain.usecase.CreateTaskUseCase
import com.fieldsyncpro.domain.usecase.DeleteTaskUseCase
import com.fieldsyncpro.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _effects = Channel<TaskDetailEffect>(Channel.BUFFERED)
    val effects: Flow<TaskDetailEffect> = _effects.receiveAsFlow()

    fun onIntent(intent: TaskDetailIntent) {
        when (intent) {
            is TaskDetailIntent.LoadTask           -> loadTask(intent.id)
            is TaskDetailIntent.UpdateTitle        -> updateTitle(intent.title)
            is TaskDetailIntent.UpdateDescription  -> updateDescription(intent.description)
            is TaskDetailIntent.UpdateVibe         -> updateVibe(intent.vibe)
            is TaskDetailIntent.UpdateStatus       -> updateStatus(intent.status)
            is TaskDetailIntent.SaveTask           -> saveTask()
            is TaskDetailIntent.DeleteTask         -> deleteTask()
        }
    }

    private fun loadTask(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            taskRepository.observeTask(id)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { task ->
                    _uiState.update { it.copy(isLoading = false, task = task) }
                }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(task = it.task?.copy(title = title)) }
    }

    private fun updateDescription(description: String) {
        _uiState.update { it.copy(task = it.task?.copy(description = description)) }
    }

    private fun updateVibe(vibe: TaskVibe) {
        _uiState.update { it.copy(task = it.task?.copy(vibe = vibe)) }
    }

    private fun updateStatus(status: TaskStatus) {
        _uiState.update { it.copy(task = it.task?.copy(status = status)) }
    }

    private fun saveTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                if (task.isLocalOnly) createTaskUseCase(task)
                else updateTaskUseCase(task)
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                _effects.send(TaskDetailEffect.ShowSnackbar("Task saved"))
                _effects.send(TaskDetailEffect.NavigateBack)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                _effects.send(TaskDetailEffect.ShowSnackbar("Failed to save task"))
            }
        }
    }

    private fun deleteTask() {
        viewModelScope.launch {
            val task = _uiState.value.task ?: return@launch
            runCatching { deleteTaskUseCase(task.id) }
                .onSuccess {
                    _effects.send(TaskDetailEffect.TaskDeleted)
                    _effects.send(TaskDetailEffect.NavigateBack)
                }
                .onFailure {
                    _effects.send(TaskDetailEffect.ShowSnackbar("Failed to delete task"))
                }
        }
    }

    /** Initialise the state for creating a brand-new task. */
    fun initNewTask(newTask: FieldTask) {
        _uiState.update { it.copy(task = newTask) }
    }
}
