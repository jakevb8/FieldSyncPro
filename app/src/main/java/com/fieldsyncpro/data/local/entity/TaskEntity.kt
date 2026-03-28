package com.fieldsyncpro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fieldsyncpro.domain.model.TaskStatus

/**
 * Room entity that persists a [com.fieldsyncpro.domain.model.FieldTask].
 *
 * [TaskVibe] is stored as a plain String via [com.fieldsyncpro.data.local.TaskVibeConverter].
 */
@Entity(tableName = "field_tasks")
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "status")
    val status: TaskStatus,

    /** Stored as one of: "Hype", "Steady", "Chill". */
    @ColumnInfo(name = "vibe")
    val vibe: String,

    @ColumnInfo(name = "last_synced")
    val lastSynced: Long,

    @ColumnInfo(name = "is_local_only")
    val isLocalOnly: Boolean
)
