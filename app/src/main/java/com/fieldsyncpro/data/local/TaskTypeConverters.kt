package com.fieldsyncpro.data.local

import androidx.room.TypeConverter
import com.fieldsyncpro.domain.model.TaskStatus
import com.fieldsyncpro.domain.model.TaskVibe

/** Room type converters for [TaskStatus] and [TaskVibe]. */
class TaskTypeConverters {

    // ── TaskStatus ────────────────────────────────────────────────────────────

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    // ── TaskVibe ──────────────────────────────────────────────────────────────

    @TypeConverter
    fun fromTaskVibe(vibe: TaskVibe): String = when (vibe) {
        is TaskVibe.Hype   -> "Hype"
        is TaskVibe.Steady -> "Steady"
        is TaskVibe.Chill  -> "Chill"
    }

    @TypeConverter
    fun toTaskVibe(value: String): TaskVibe = when (value) {
        "Hype"   -> TaskVibe.Hype
        "Steady" -> TaskVibe.Steady
        "Chill"  -> TaskVibe.Chill
        else     -> throw IllegalArgumentException("Unknown TaskVibe: $value")
    }
}
