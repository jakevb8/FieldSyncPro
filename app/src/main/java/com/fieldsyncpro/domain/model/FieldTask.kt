package com.fieldsyncpro.domain.model

/**
 * Represents the priority/urgency level of a [FieldTask].
 * Named "vibe" to match field-technician vernacular used in the spec.
 */
sealed interface TaskVibe {
    /** Urgent / immediate attention required. */
    object Hype : TaskVibe

    /** Standard / normal workload. */
    object Steady : TaskVibe

    /** Low priority / can wait. */
    object Chill : TaskVibe
}

/** All possible lifecycle states for a [FieldTask]. */
enum class TaskStatus {
    PENDING,
    SYNCING,
    COMPLETED,
    CONFLICT
}

/**
 * Core domain model for a field task.
 *
 * @param id           Stable unique identifier (UUID string).
 * @param title        Short human-readable title.
 * @param description  Full description of the work to be done.
 * @param status       Current sync / lifecycle state.
 * @param vibe         Priority level expressed as [TaskVibe].
 * @param lastSynced   Epoch-millis timestamp of last successful server sync (0 = never synced).
 * @param isLocalOnly  `true` when the task was created offline and hasn't been pushed yet
 *                     (optimistic UI flag).
 */
data class FieldTask(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val vibe: TaskVibe,
    val lastSynced: Long,
    val isLocalOnly: Boolean
)
