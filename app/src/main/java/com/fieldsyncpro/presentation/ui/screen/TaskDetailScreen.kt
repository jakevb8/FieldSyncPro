package com.fieldsyncpro.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.presentation.viewmodel.*
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String?,          // null means "create new"
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Init state
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.onIntent(TaskDetailIntent.LoadTask(taskId))
        } else {
            viewModel.initNewTask(
                FieldTask(
                    id          = UUID.randomUUID().toString(),
                    title       = "",
                    description = "",
                    status      = TaskStatus.PENDING,
                    vibe        = TaskVibe.Steady,
                    lastSynced  = 0L,
                    isLocalOnly = true
                )
            )
        }
    }

    // One-shot effects
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TaskDetailEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is TaskDetailEffect.NavigateBack -> onNavigateBack()
                is TaskDetailEffect.TaskDeleted  -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "New Task" else "Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (taskId != null) {
                        IconButton(onClick = { viewModel.onIntent(TaskDetailIntent.DeleteTask) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            uiState.task != null -> {
                TaskDetailForm(
                    task            = uiState.task!!,
                    isSaving        = uiState.isSaving,
                    onTitleChange   = { viewModel.onIntent(TaskDetailIntent.UpdateTitle(it)) },
                    onDescChange    = { viewModel.onIntent(TaskDetailIntent.UpdateDescription(it)) },
                    onVibeChange    = { viewModel.onIntent(TaskDetailIntent.UpdateVibe(it)) },
                    onStatusChange  = { viewModel.onIntent(TaskDetailIntent.UpdateStatus(it)) },
                    onSave          = { viewModel.onIntent(TaskDetailIntent.SaveTask) },
                    modifier        = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TaskDetailForm(
    task: FieldTask,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onVibeChange: (TaskVibe) -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value         = task.title,
            onValueChange = onTitleChange,
            label         = { Text("Title") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true
        )

        OutlinedTextField(
            value         = task.description,
            onValueChange = onDescChange,
            label         = { Text("Description") },
            modifier      = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines      = 5
        )

        // Vibe selector
        Text("Priority (Vibe)", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(TaskVibe.Hype, TaskVibe.Steady, TaskVibe.Chill).forEach { vibe ->
                val label = when (vibe) {
                    is TaskVibe.Hype   -> "Hype"
                    is TaskVibe.Steady -> "Steady"
                    is TaskVibe.Chill  -> "Chill"
                }
                FilterChip(
                    selected = task.vibe::class == vibe::class,
                    onClick  = { onVibeChange(vibe) },
                    label    = { Text(label) }
                )
            }
        }

        // Status selector (only shown when editing)
        if (!task.isLocalOnly) {
            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskStatus.values().forEach { status ->
                    FilterChip(
                        selected = task.status == status,
                        onClick  = { onStatusChange(status) },
                        label    = { Text(status.name) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick  = onSave,
            enabled  = !isSaving && task.title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Task")
            }
        }
    }
}
