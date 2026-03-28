package com.fieldsyncpro.presentation.viewmodel

import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe

// ── UI State ──────────────────────────────────────────────────────────────────

data class TaskDetailUiState(
    val task: FieldTask? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

// ── Intents ───────────────────────────────────────────────────────────────────

sealed interface TaskDetailIntent {
    data class LoadTask(val id: String)  : TaskDetailIntent
    data class UpdateTitle(val title: String)  : TaskDetailIntent
    data class UpdateDescription(val description: String) : TaskDetailIntent
    data class UpdateVibe(val vibe: TaskVibe)  : TaskDetailIntent
    data class UpdateStatus(val status: TaskStatus) : TaskDetailIntent
    object SaveTask   : TaskDetailIntent
    object DeleteTask : TaskDetailIntent
}

// ── Effects ───────────────────────────────────────────────────────────────────

sealed interface TaskDetailEffect {
    data class ShowSnackbar(val message: String) : TaskDetailEffect
    object NavigateBack : TaskDetailEffect
    object TaskDeleted  : TaskDetailEffect
}
