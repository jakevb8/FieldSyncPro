package com.fieldsyncpro.presentation.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fieldsyncpro.presentation.ui.component.TaskCard
import com.fieldsyncpro.presentation.viewmodel.*
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is TaskListEffect.ShowSnackbar     -> snackbarHostState.showSnackbar(effect.message)
                is TaskListEffect.NavigateToDetail -> onNavigateToDetail(effect.taskId)
                else                               -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FieldSync Pro") },
                actions = {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.onIntent(TaskListIntent.TriggerSync) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync tasks")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Create task")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Vibe filter chips
            VibeFilterRow(
                activeFilter = uiState.activeFilter,
                onFilterSelected = { filter ->
                    viewModel.onIntent(TaskListIntent.FilterByVibe(filter))
                }
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.tasks.isEmpty() -> {
                    EmptyStateMessage()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(
                            items = uiState.tasks,
                            key   = { it.id }
                        ) { task ->
                            TaskCard(
                                task        = task,
                                onCardClick = { id -> onNavigateToDetail(id) },
                                onComplete  = { id -> viewModel.onIntent(TaskListIntent.CompleteTask(id)) },
                                onDelete    = { id -> viewModel.onIntent(TaskListIntent.DeleteTask(id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VibeFilterRow(
    activeFilter: TaskVibeFilter,
    onFilterSelected: (TaskVibeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TaskVibeFilter.values().forEach { filter ->
            FilterChip(
                selected = activeFilter == filter,
                onClick  = { onFilterSelected(filter) },
                label    = { Text(filter.name) }
            )
        }
    }
}

@Composable
private fun EmptyStateMessage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text  = "No tasks yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text  = "Tap + to create your first field task",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
