package com.fieldsyncpro.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe
import com.fieldsyncpro.presentation.ui.theme.VibeChillColor
import com.fieldsyncpro.presentation.ui.theme.VibeHypeColor
import com.fieldsyncpro.presentation.ui.theme.VibeSteadyColor
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card representing a single [FieldTask] in the task list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: FieldTask,
    onCardClick: (String) -> Unit,
    onComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick   = { onCardClick(task.id) },
        modifier  = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = task.title,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f)
                )
                VibeChip(vibe = task.vibe)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text     = task.description,
                style    = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskStatusChip(status = task.status, isLocalOnly = task.isLocalOnly)

                Row {
                    if (task.status != TaskStatus.COMPLETED) {
                        IconButton(onClick = { onComplete(task.id) }) {
                            Icon(
                                imageVector        = Icons.Default.Done,
                                contentDescription = "Mark complete",
                                tint               = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onDelete(task.id) }) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = "Delete task",
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (task.lastSynced > 0) {
                Text(
                    text  = "Synced: ${formatTimestamp(task.lastSynced)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun VibeChip(vibe: TaskVibe, modifier: Modifier = Modifier) {
    val (label, color) = when (vibe) {
        is TaskVibe.Hype   -> "HYPE"   to VibeHypeColor
        is TaskVibe.Steady -> "STEADY" to VibeSteadyColor
        is TaskVibe.Chill  -> "CHILL"  to VibeChillColor
    }
    SuggestionChip(
        onClick = {},
        label   = {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        },
        modifier = modifier
    )
}

@Composable
fun TaskStatusChip(status: TaskStatus, isLocalOnly: Boolean, modifier: Modifier = Modifier) {
    val label = if (isLocalOnly) "LOCAL" else status.name
    val containerColor = when {
        isLocalOnly            -> MaterialTheme.colorScheme.tertiaryContainer
        status == TaskStatus.COMPLETED -> MaterialTheme.colorScheme.secondaryContainer
        status == TaskStatus.CONFLICT  -> MaterialTheme.colorScheme.errorContainer
        status == TaskStatus.SYNCING   -> MaterialTheme.colorScheme.primaryContainer
        else                           -> MaterialTheme.colorScheme.surfaceVariant
    }
    Surface(
        color  = containerColor,
        shape  = MaterialTheme.shapes.small,
        modifier = modifier.padding(end = 4.dp)
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun formatTimestamp(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}
