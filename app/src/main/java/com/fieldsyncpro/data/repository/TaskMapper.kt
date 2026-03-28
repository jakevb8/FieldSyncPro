package com.fieldsyncpro.data.repository

import com.fieldsyncpro.data.local.entity.TaskEntity
import com.fieldsyncpro.data.remote.dto.TaskDto
import com.fieldsyncpro.domain.model.FieldTask
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe

/** Stateless mapper between all three task representations. */
object TaskMapper {

    // ── FieldTask ↔ TaskEntity ────────────────────────────────────────────────

    fun FieldTask.toEntity(): TaskEntity = TaskEntity(
        id          = id,
        title       = title,
        description = description,
        status      = status,
        vibe        = vibeToString(vibe),
        lastSynced  = lastSynced,
        isLocalOnly = isLocalOnly
    )

    fun TaskEntity.toDomain(): FieldTask = FieldTask(
        id          = id,
        title       = title,
        description = description,
        status      = status,
        vibe        = stringToVibe(vibe),
        lastSynced  = lastSynced,
        isLocalOnly = isLocalOnly
    )

    // ── FieldTask ↔ TaskDto ───────────────────────────────────────────────────

    fun FieldTask.toDto(): TaskDto = TaskDto(
        id          = id,
        title       = title,
        description = description,
        status      = status.name,
        vibe        = vibeToString(vibe),
        lastSynced  = lastSynced
    )

    fun TaskDto.toDomain(): FieldTask = FieldTask(
        id          = id,
        title       = title,
        description = description,
        status      = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
        vibe        = stringToVibe(vibe),
        lastSynced  = lastSynced,
        isLocalOnly = false
    )

    fun TaskDto.toEntity(): TaskEntity = toDomain().toEntity()

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun vibeToString(vibe: TaskVibe): String = when (vibe) {
        is TaskVibe.Hype   -> "Hype"
        is TaskVibe.Steady -> "Steady"
        is TaskVibe.Chill  -> "Chill"
    }

    fun stringToVibe(value: String): TaskVibe = when (value) {
        "Hype"   -> TaskVibe.Hype
        "Steady" -> TaskVibe.Steady
        "Chill"  -> TaskVibe.Chill
        else     -> TaskVibe.Steady
    }
}
