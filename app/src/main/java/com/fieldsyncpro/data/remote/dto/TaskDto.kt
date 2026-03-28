package com.fieldsyncpro.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON DTO returned/sent by the FieldSync Pro REST API.
 *
 * Maps 1-to-1 with the server contract; domain mapping happens in the
 * repository via [com.fieldsyncpro.data.repository.TaskMapper].
 */
@Serializable
data class TaskDto(
    @SerialName("id")          val id: String,
    @SerialName("title")       val title: String,
    @SerialName("description") val description: String,
    @SerialName("status")      val status: String,
    @SerialName("vibe")        val vibe: String,
    @SerialName("last_synced") val lastSynced: Long
)
