package com.fieldsyncpro.presentation.viewmodel

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskVibe

// ── UI State ──────────────────────────────────────────────────────────────────

data class TaskListUiState(
    val tasks: List<FieldTask> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val activeFilter: TaskVibeFilter = TaskVibeFilter.ALL
)

enum class TaskVibeFilter { ALL, HYPE, STEADY, CHILL }

// ── Intents ───────────────────────────────────────────────────────────────────

sealed interface TaskListIntent {
    object LoadTasks       : TaskListIntent
    object TriggerSync     : TaskListIntent
    data class DeleteTask(val id: String)  : TaskListIntent
    data class FilterByVibe(val filter: TaskVibeFilter) : TaskListIntent
    data class CompleteTask(val id: String) : TaskListIntent
}

// ── Effects (one-shot events) ─────────────────────────────────────────────────

sealed interface TaskListEffect {
    data class ShowSnackbar(val message: String)  : TaskListEffect
    data class NavigateToDetail(val taskId: String) : TaskListEffect
    object SyncCompleted  : TaskListEffect
    object SyncFailed     : TaskListEffect
}
